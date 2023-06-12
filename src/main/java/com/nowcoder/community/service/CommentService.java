package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    //增加评论的业务逻辑
    /**
     * 增加评论的同时，需要修改帖子里的评论数量字段，所以需要事务管理。
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        //判空
        if(comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //增加帖子评论
        int rows = commentMapper.insertComment(comment);
        //更新帖子评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            //在comment表中查出某个帖子评论数量
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());//comment.getEntityId()帖子id
            //在discuss_post表中更新某个帖子评论数量
            discussPostService.updateCommentCount(comment.getEntityId(), count);//comment.getEntityId()帖子id
        }
        return rows;
    }
}
