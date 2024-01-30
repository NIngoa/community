package com.nowcoder.community.service;

import com.nowcoder.community.entity.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService {

     List<Message>findConversationList(int userId, int offset, int limit);

     int findConversationCount(int userId);

     List<Message>findMessageList(String conversationId, int offset, int limit);

     int findMessageCount(String conversationId);

     int findLetterUnreadCount(int userId,String conversationId);

     int insertMessage(Message message);

    int updateMessageReadStatus(List<Integer> ids);

    Message selectLatestNotification(int userId,String topic);

    int selectNotificationCount(int userId,String topic);

    int selectNotificationUnreadCount(int userId,String topic);

    List<Message> selectNotificationList(int userId, String conversationId, int offset, int limit);
}
