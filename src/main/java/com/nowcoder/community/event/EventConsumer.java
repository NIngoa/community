package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

@Component
@Slf4j
public class EventConsumer implements CommunityConstant {

    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Value("${wk.image.command}")
    private String wkImageCommand;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW},groupId = "Community-consumer-group")
    public void consume(ConsumerRecord record) {
        if (record==null||record.value()==null){
            log.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event==null){
            log.error("消息内容解析失败!");
            return;
        }

        Message message=new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(LocalDateTime.now());
        Map<String,Object>content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.insertMessage(message);
    }

    @KafkaListener(topics = TOPIC_PUBLISH,groupId = "Community-consumer-group")
    public void consumePublish(ConsumerRecord record) {
        if (record==null||record.value()==null){
            log.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event==null){
            log.error("消息内容解析失败!");
            return;
        }
        DiscussPost discussPost = discussPostService.selectDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    @KafkaListener(topics = TOPIC_DELETE,groupId = "Community-consumer-group")
    public void consumeDelete(ConsumerRecord record) {
        if (record==null||record.value()==null){
            log.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event==null){
            log.error("消息内容解析失败!");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }
    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShare(ConsumerRecord record) {
        if (record==null||record.value()==null){
            log.error("消息内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event==null){
            log.error("消息内容解析失败!");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String filename = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd=wkImageCommand+" --quality 75 "
                +htmlUrl+" " +wkImageStorage+"/" +filename+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            log.info("生成长图成功:"+cmd);
        } catch (IOException e) {
            log.error("生成长图失败:"+e.getMessage());
        }
        //启用定时器，监视该图片，一旦生成了，则上传至七牛云
        UploadTask task=new UploadTask(filename,suffix);
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }
    class UploadTask implements Runnable{

        //文件名称
        private String fileName;
        //文件后缀
        private String suffix;
        //启动任务的返回值
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
        private int uploadTimes;


        public void setFuture(Future future) {
            this.future = future;
        }


        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            //生成图片失败
            if (System.currentTimeMillis()-startTime>30000){
                log.error("生成长图失败，超过30秒，取消上传:{}",fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if (uploadTimes>3){
                log.error("生成长图失败，超过10次，取消上传:{}",fileName);
                future.cancel(true);
                return;
            }
            //上传图片
            String path=wkImageStorage+"/"+fileName+suffix;
            File file=new File(path);
            if (file.exists()){
                log.info("开始第{}次上传{}",++uploadTimes,fileName);
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonStr(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //指定上传机房
                UploadManager manager=new UploadManager(new Configuration(Zone.zone1()));
                try {
                    //开始上传图片
                    Response response=manager.put(path, fileName, uploadToken, null,"image/"+suffix,false);
                    //处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if (!jsonObject.get("code").toString().equals("0")||jsonObject==null||jsonObject.get("code")==null){
                        log.info("第{}次上传失败{}",uploadTimes,fileName);
                    }else {
                        log.info("第{}次上传成功{}",uploadTimes,fileName);
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    log.info("第{}次上传失败{}",uploadTimes,fileName);
                }
            }else {
                log.info("等待图片生成:{}",fileName);
            }
        }
    }
}
