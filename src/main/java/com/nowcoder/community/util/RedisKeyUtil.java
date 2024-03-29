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

    //验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //登录凭证
    private static final String PREFIX_TICKET = "ticket";
    //缓存用户信息
    private static final String PREFIX_USER = "user";

    //独立访客UV：在一定时间段，访问网站的个体的数量
    private static final String PREFIX_UV = "uv";

    //日活跃用户DAU：每天与网站互动的用户的数量
    private static final String PREFIX_DAU = "dau";


    //帖子热度
    private static final String PREFIX_POST = "post";

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

    // 登陆验证吗（重构）
    public static String getKaptchaKey(String owner) {//owner临时凭证，用来标识当前待登陆用户，很快失效
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }


    //单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日DAU
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //区间DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }


    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
