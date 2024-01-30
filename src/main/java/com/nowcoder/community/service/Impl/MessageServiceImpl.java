package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> findConversationList(int userId, int offset, int limit) {
        return messageMapper.selectConversationList(userId, offset, limit);
    }

    @Override
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> findMessageList(String conversationId, int offset, int limit) {
        return messageMapper.selectMessageList(conversationId, offset, limit);
    }

    @Override
    public int findMessageCount(String conversationId) {
        return messageMapper.selectMessageCount(conversationId);
    }

    @Override
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    @Override
    public int insertMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    @Override
    public int updateMessageReadStatus(List<Integer> ids) {
       return  messageMapper.updateMessageReadStatus(ids,1);
    }

    @Override
    public Message selectLatestNotification(int userId, String topic) {
        return messageMapper.selectLatestNotification(userId,topic);
    }

    @Override
    public int selectNotificationCount(int userId, String topic) {
        return messageMapper.selectNotificationCount(userId,topic);
    }

    @Override
    public int selectNotificationUnreadCount(int userId, String topic) {
        return messageMapper.selectNotificationUnreadCount(userId,topic);
    }

    @Override
    public List<Message> selectNotificationList(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotificationList(userId,topic,offset,limit);
    }
}
