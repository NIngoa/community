package com.nowcoder.community.utils;

public class RedisUtil {
    public final static String SPLIT = ":";
    public final static String PREFIX_ENTITY_LIKE="like:entity";
    public final static String PREFIX_USER_LIKE="like:user";

    public static String getEntityLikeKey(Integer entityId,Integer entityType) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
    public static String getUserLikeKey(Integer userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
