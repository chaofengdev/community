package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 发布帖子
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
        discussPostService.addDiscussPost(post);//ps：这里要求主键自增后，自动回填到对象中，因为后面用到了post.getId()

        // 触发发帖事件
        //事件对象
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        //触发事件
        eventProducer.fireEvent(event);

        //返回插入成功的消息--json字符串
        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 查看某个帖子详情数据
     * 以及帖子下方的评论数据，还需要实现评论数据的分页
     * @param discussPostId
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {//这里需要返回具体帖子详情的页面，所以不需要@ResponseBody注解
        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //帖子作者--可以在数据库中关联查询，也可以在这里单独查询user再添加到model中
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        //新增：点赞相关信息--帖子详情
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态 这里要考虑用户没有登录的情况，直接置为0，否则去查询点赞状态。
        int likeStatus = hostHolder.getUsers() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //帖子的回复
        //评论的分页信息--这里实现了分页查询的功能，这里可以体会出Page类设置的好处，能够直接复用前端的分页组件，不需要修改后端任何代码。
        page.setLimit(5);//只显示5条评论，显示效果更好
        page.setPath("/discuss/detail/" + discussPostId);//访问路径
        page.setRows(post.getCommentCount());//discuss_post的冗余字段，直接查出某个帖子下评论的总数量

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());//支持分页查询
        //评论VO列表 关于java中VO的解释：https://www.cnblogs.com/yxnchinahlj/archive/2012/02/24/2366110.html
        List<Map<String, Object>> commentVoList = new ArrayList<>();//commentVoList里的Vo表示view object，即显示对象
        if(commentList != null) {
            for(Comment comment : commentList) {
                //评论VO
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);//评论
                //评论的作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));//评论的作者

                //新增：点赞相关信息--帖子评论
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态 这里要考虑用户没有登录的情况，直接置为0，否则去查询点赞状态。
                likeStatus = hostHolder.getUsers() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //回复列表--这里需要理解，帖子的评论下还有评论，此为回复。这里感觉数据库表设计的不是很合理，希望后面得到改进。
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);//这里本质上不需要分页
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for(Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标--这里和前端需要的数据有关，前端需要回复目标的数据
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //新增：点赞相关信息--回复
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态 这里要考虑用户没有登录的情况，直接置为0，否则去查询点赞状态。
                        likeStatus = hostHolder.getUsers() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUsers().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        //返回模板路径
        return "/site/discuss-detail";
    }

    //置顶、取消置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        //修改帖子状态
        //discussPostService.updateType(id, 1);//帖子类型0表示普通，1表示置顶

        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 获取置顶状态 1为置顶，0为正常状态 1^1=0 0^1=1 这里没有考虑拉黑状态
        int new_type = post.getType()^1;
        discussPostService.updateType(id, new_type);

        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("type", new_type);

        // 触发发帖事件--这里是为了同步帖子信息到Elasticsearch中。
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "操作失败了，联系开发人员解决问题吧！", map);//0表示成功
    }

    //加精、取消加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        //修改帖子状态
        //discussPostService.updateStatus(id, 1);//帖子状态0表示正常，1表示加精，2表示拉黑

        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 获取加精状态 1为加精，0为正常， 1^1=0, 0^1=1
        int new_status = post.getStatus()^1;
        discussPostService.updateStatus(id, new_status);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("status", new_status);

        // 触发发帖事件--这里是为了同步帖子信息到Elasticsearch中。
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "操作失败了，联系开发人员解决问题吧！", map);//0表示成功
    }

    //删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        //修改帖子状态
        discussPostService.updateStatus(id, 2);//帖子状态0表示正常，1表示加精，2表示拉黑（删除）

        // 触发删帖事件--这里是为了删除Elasticsearch中的帖子信息。
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);//0表示成功
    }
}
