package com.nowcoder.community.service;

import java.util.List;
import java.util.Map;

public interface FollowService {

    void follow(Integer userId, Integer entityType, Integer entityId);

    void unfollow(Integer userId, Integer entityType, Integer entityId);

    Long findFolloweeCount(Integer userId, Integer entityType);

    Long findFollowerCount(Integer entityType, Integer entityId);

    Boolean isFollow(Integer userId, Integer entityType, Integer entityId);

    List<Map<String,Object>> findFollowee(int userId, int offset, int limit);

    List<Map<String,Object>> findFollower(int userId, int offset, int limit);
}
