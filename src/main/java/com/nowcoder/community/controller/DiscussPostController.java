package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 增加某个帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        //登录校验
        User user = hostHolder.getUsers();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录");
        }
        //实例化帖子对象
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        //在数据库中插入帖子对象
        discussPostService.addDiscussPost(post);
        //返回插入成功的消息--json字符串
        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 查看某个帖子
     * @param discussPost
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPost, Model model) {//这里需要返回具体帖子详情的页面，所以不需要@ResponseBody注解
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPost);
        model.addAttribute("post", post);
        //帖子作者--可以在数据库中关联查询，也可以在这里单独查询user再添加到model中
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //帖子的回复相关内容--待补充
        //返回模板路径
        return "/site/discuss-detail";
    }


}
