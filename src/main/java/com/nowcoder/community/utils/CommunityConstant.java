package com.nowcoder.community.utils;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * 激活失败
     */
    int ACTIVATION_FAIL = 2;

    /**
     * 默认过期时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600*24;
    /**
     * 记住我过期时间
     */
    int REMEMBER_ME_EXPIRED_SECONDS = 3600*24*30;
}
