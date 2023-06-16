package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * 根据传入的用户ID（userId）、实体类型（entityType）和实体ID（entityId），
     * 在Redis中维护一个点赞的集合，并根据用户的操作进行添加或删除操作。
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId, int entityType, int entityId) {
        //根据实体类型和实体ID生成一个在Redis中用于存储点赞信息的键
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //使用isMember()方法检查该集合中是否已经包含了该用户的ID
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember) {
            //该用户已经点赞过，那么使用opsForSet().remove()方法从集合中移除该用户的ID。
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        }else {
            //该用户尚未点赞过，那么使用opsForSet().add()方法将该用户的ID添加到集合中。
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    /**
     * 查询某实体点赞的数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实体的点赞状态
     * 这里返回值不是boolean而是int，是因为考虑到今后业务扩展，赞之外还有踩的业务需求。
     * 1表示已赞，0表示未赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
