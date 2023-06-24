package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //帖子
        comment.setUserId(hostHolder.getUsers().getId());//用户不一定登录，所以user可能为空，统一异常处理
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        //增加评论
        commentService.addComment(comment);

        // 利用kafka实现异步处理系统通知
        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);//帖子id 为了前端能找到对应的帖子
        //获取作者  因为可以评论帖子，也可以评论评论（此时称对评论的评论为回复）
        if(comment.getEntityType() == ENTITY_TYPE_POST) {// 如果评论关联的实体是帖子
            // 设置事件关联的实体用户ID为帖子作者的ID
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT) {// 如果评论关联的实体是评论
            // 设置事件关联的实体用户ID为评论作者的ID
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        // 触发事件，将事件发送到事件队列进行处理
        eventProducer.fireEvent(event);

        //重定向到帖子详情页面
        //关于重定向，我们评论后还要再查看这条帖子详情，就相当于再发送一个查看帖子详情的请求
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
