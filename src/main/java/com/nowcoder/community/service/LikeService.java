package com.nowcoder.community.service;

public interface LikeService {
    public void like(int userId,Integer entityType,Integer entityId ,Integer likeUserId);

    public long findEntityLikeCount(Integer entityType, Integer entityId);

    public Integer findEntityLikeStatus(int userId, Integer entityType, Integer entityId);

    int selectLikeCount(int userId);
}
