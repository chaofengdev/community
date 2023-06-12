package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //帖子
        comment.setUserId(hostHolder.getUsers().getId());//用户不一定登录，所以user可能为空，后面统一异常处理
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        //增加帖子
        commentService.addComment(comment);
        //重定向到帖子详情页面
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
