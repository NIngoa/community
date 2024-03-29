package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表,针对每条会话只返回一条最新消息
    List<Message> selectConversationList(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectMessageList(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectMessageCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    //新增消息
    int insertMessage(Message message);

    //更新消息状态
    int updateMessageReadStatus(List<Integer> ids, int status);

    //查询未读通知数量
    int selectNotificationUnreadCount(int userId,String topic);

    //查询某个主题的通知数量
    int selectNotificationCount(int userId, String topic);

    //查询某个主题下最新的通知
    Message selectLatestNotification(int userId,String topic);

    List<Message> selectNotificationList(int userId, String topic, int offset, int limit);
}
