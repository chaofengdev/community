package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 该控制器类接收前端发起的点赞请求，调用 likeService 处理点赞逻辑，
 * 并返回点赞数量和点赞状态的 JSON 格式数据给客户端。
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {//重构，增加postId
        User user = hostHolder.getUsers();
        //...
        //这里可以使用拦截器，进行用户的校验工作，即只能授权已登录用户进行点赞；后面会通过spring security进行重构。

        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        //数量
        Long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType, entityId);



        //返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if(likeStatus == 1) {//只有点赞会触发，取消点赞不触发。
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUsers().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);//重构like方法，要求传入点赞的帖子id 为了前端能找到对应的帖子
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数 只有对帖子的点赞会增加帖子的热度
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);//正确无提示，与前端页面需求有关。
    }
}
