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
    int DEFAULT_EXPIRED_SECONDS = 3600 * 24;
    /**
     * 记住我过期时间
     */
    int REMEMBER_ME_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;
    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;
    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题类型：评论
     */
    String TOPIC_COMMENT = "comment";
    /**
     * 主题类型：点赞
     */
    String TOPIC_LIKE = "like";
    /**
     * 主题类型：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户id
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 主题类型：发布
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题类型：删除
     */
    String TOPIC_DELETE = "delete";

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR = "moderator";

    /**
     * 置顶
     */
    int DISCUSS_POST_TOP = 1;

    /**
     * 加精
     */
    int DISCUSS_POST_WONDERFUL = 1;

    /**
     * 删除
     */
    int DISCUSS_POST_DELETE = 2;
}
