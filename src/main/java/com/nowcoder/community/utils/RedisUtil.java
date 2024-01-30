package com.nowcoder.community.utils;

public class RedisUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE= "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    /**
     * 实体点赞
     * @param entityId
     * @param entityType
     * @return
     */
    public static String getEntityLikeKey(Integer entityId, Integer entityType) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 用户点赞
     * @param userId
     * @return
     */
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

    //登陆验证码
    public static String getPrefixKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(Integer userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
