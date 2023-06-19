package com.nowcoder.community.util;

/**
 * 用于生成Redis键的实用工具类。
 * 它包含了一些静态方法，用于生成在社区类应用中常用的键，用于实现点赞、关注和追踪粉丝等功能。
 */
public class RedisKeyUtil {

    //点赞 like
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    //关注 follower followee
    private static final String PREFIX_FOLLOWEE = "followee";//关注
    private static final String PREFIX_FOLLOWER = "follower";//粉丝

    // 某个实体的（收到的）赞
    // like:entity:entityType:entityId -> set(userId)
    // 这里用set集合装userId，是为了统计赞的个数和查找点赞用户
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的（被点的）赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个[用户]关注的[实体]
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个[实体]拥有的[粉丝]
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
