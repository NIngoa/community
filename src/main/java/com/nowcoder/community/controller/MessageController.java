package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessageAware;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {
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
        page.setPath("/letter/detail/"+conversationId);
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
        }return ids;
    }

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


}
