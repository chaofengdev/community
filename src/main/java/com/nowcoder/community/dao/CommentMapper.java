package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //（分页）查询评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //查询（特定类型）评论的数量
    int selectCountByEntity(int entityType, int entityId);

    //增加评论
    int insertComment(Comment comment);

}
