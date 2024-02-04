package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.mail.MessageAware;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 获取消息列表
     *
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Page page, Model model) {
        User user = hostHolder.getUser();
        int userId = user.getId();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(userId));
        model.addAttribute("page", page);
        //消息列表
        List<Message> conversationList = messageService.findConversationList(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("letterCount", messageService.findMessageCount(message.getConversationId()));
                map.put("unreadLetterCount", messageService.findLetterUnreadCount(userId, message.getConversationId()));
                int targetId = userId == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversationVoList.add(map);
            }
        }
        model.addAttribute("conversationVoList", conversationVoList);
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCountTotal", letterUnreadCount);
        int notificationUnreadCount = messageService.selectNotificationUnreadCount(user.getId(), null);
        model.addAttribute("notificationUnreadCountTotal", notificationUnreadCount);
        return "/site/letter";
    }

    /**
     * 获取私信详情
     *
     * @param conversationId
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable String conversationId, Model model, Page page) {
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findMessageCount(conversationId));
        model.addAttribute("page", page);
        //私信列表
        List<Message> messageList = messageService.findMessageList(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> messageVoList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("replier", userService.findUserById(message.getFromId()));
                messageVoList.add(map);
            }
        }
        model.addAttribute("messageVoList", messageVoList);
        assert messageList != null;
        Message message = messageList.get(0);
        int target = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
        model.addAttribute("target", userService.findUserById(target));

        List<Integer> ids = getIds(messageList);
        if (!ids.isEmpty()) {
            messageService.updateMessageReadStatus(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getIds(List<Message> messageList) {
        List<Integer> ids = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 发送私信
     *
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User user = userService.findUserByUsername(toName);
        if (user == null) {
            return CommunityUtil.getJsonStr(1, "用户不存在");
        }
        Message message = new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(user.getId());
        if (message.getFromId() > message.getToId()) {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        } else {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        message.setCreateTime(LocalDateTime.now());
        messageService.insertMessage(message);
        return CommunityUtil.getJsonStr(0, "发送成功");
    }

    /**
     * 获取通知列表
     *
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        //查询评论类型的通知
        Message message = messageService.selectLatestNotification(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.selectNotificationCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", count);
            int unreadCount = messageService.selectNotificationUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unreadCount", unreadCount);
            model.addAttribute("commentNotification", messageVo);
        }


        //查询关注类型的通知
        message = messageService.selectLatestNotification(user.getId(), TOPIC_FOLLOW);

        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            int count = messageService.selectNotificationCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count", count);
            int unreadCount = messageService.selectNotificationUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unreadCount", unreadCount);
            model.addAttribute("followNotification", messageVo);
        }


        //查询点赞类型的通知
        message = messageService.selectLatestNotification(user.getId(), TOPIC_LIKE);

        if (message != null) {
            Map<String, Object> messageVo = new HashMap<>();
            messageVo.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            int count = messageService.selectNotificationCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count", count);
            int unreadCount = messageService.selectNotificationUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unreadCount", unreadCount);
            model.addAttribute("likeNotification", messageVo);
        }


        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCountTotal", letterUnreadCount);
        int notificationUnreadCount = messageService.selectNotificationUnreadCount(user.getId(), null);
        model.addAttribute("notificationUnreadCountTotal", notificationUnreadCount);
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable String topic, Model model, Page page) {
        User user = hostHolder.getUser();
        page.setRows(messageService.selectNotificationCount(user.getId(), topic));
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);

        List<Message> notificationList = messageService.selectNotificationList(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> notificationVoList = new ArrayList<>();
        if (notificationList != null) {
            for (Message notice : notificationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                //解析消息内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                //通知作者
                User sysUser = userService.findUserById(notice.getFromId());
                map.put("sysUser", sysUser);
                notificationVoList.add(map);
            }
        }
        model.addAttribute("notices", notificationVoList);
        //设置已读
        List<Integer> ids = getIds(notificationList);

        if (ids.size() > 0){
            messageService.updateMessageReadStatus(ids);
        }
        return "/site/notice-detail";
    }
}
