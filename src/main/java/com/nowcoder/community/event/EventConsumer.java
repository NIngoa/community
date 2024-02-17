package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.utils.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    @Value("${wk.image.command}")
    private String wkImageCommand;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
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
    }
}
