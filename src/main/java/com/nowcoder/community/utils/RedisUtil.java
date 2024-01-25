package com.nowcoder.community.utils;

public class RedisUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE= "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    public static String getEntityLikeKey(Integer entityId, Integer entityType) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getUserLikeKey(Integer userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 我关注的
     * @param userId
     * @return
     */
    public static String getFolloweeKey(Integer userId,Integer entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 粉丝
     * @param entityId
     * @return
     */
    public static String getFollowerKey(Integer entityId,Integer entityType) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
