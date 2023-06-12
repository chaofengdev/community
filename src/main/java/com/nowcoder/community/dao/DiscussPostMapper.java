package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper  //这里不能遗漏，表示将DisscussPostMapper注册为bean
public interface DiscussPostMapper {
    //根据用户id查询帖子，返回帖子集合，当id为0时，表示查询所有的帖子
    //输入用户id、起始帖子id、每页帖子数量
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //根据用户id查询帖子数量，返回整型值
    //当id为0时，表示查询所有的帖子数量
    //关于注解@Param的使用，有以下几个场景：
    //1.方法有多个参数，需要@Param注解
    //2.方法参数取别名
    //3.XML中的SQL使用了$，那么参数中也需要 @Param注解
    //4.动态SQL使用了参数作为变量，也需要@Param注解
    int selectDiscussPostRows(@Param("userId") int userId);

    //增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子
    DiscussPost selectDiscussPostById(int id);

    //修改--增加或者删除评论后，修改某个帖子的评论数量
    int updateCommentCount(int id, int commentCount);
}
