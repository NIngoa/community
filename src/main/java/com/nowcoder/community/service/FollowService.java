package com.nowcoder.community.service;

public interface FollowService {

    void follow(Integer userId, Integer entityType, Integer entityId);

    void unfollow(Integer userId, Integer entityType, Integer entityId);

    Long findFolloweeCount(Integer userId, Integer entityType);

    Long findFollowerCount(Integer entityType, Integer entityId);

    Boolean isFollow(Integer userId, Integer entityType, Integer entityId);
}
