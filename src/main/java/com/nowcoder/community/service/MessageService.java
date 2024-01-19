package com.nowcoder.community.service;

import com.nowcoder.community.entity.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MessageService {

    public List<Message>findConversationList(int userId, int offset, int limit);

    public int findConversationCount(int userId);

    public List<Message>findMessageList(String conversationId, int offset, int limit);

    public int findMessageCount(String conversationId);

    public int findLetterUnreadCount(int userId,String conversationId);

    public int insertMessage(Message message);

    int updateMessageReadStatus(List<Integer> ids);
}
