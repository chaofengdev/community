package com.nowcoder.community.util;

/**
 * 激活状态
 */
public interface CommunityConstant {

    //激活相关的常量
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;//快捷键：ctrl+shift+u 将字符串所有字符转换为大写字符

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    //登录相关的常量
    /**
     * 默认状态的登录凭证的超时时间 12小时
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;//3600s * 12 == 12h

    /**
     * 记住状态下的登录凭证的超时时间 100天
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

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
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    //系统用户id
    int SYSTEM_USER_ID = 1;
}
