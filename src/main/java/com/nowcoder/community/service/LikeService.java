package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * 根据传入的用户ID（userId）、实体类型（entityType）和实体ID（entityId），
     * 在Redis中维护一个点赞的集合，并根据用户的操作进行添加或删除操作。
     * @param userId 当前用户id
     * @param entityType
     * @param entityId
     * @param entityUserId 被点赞帖子或回复的用户id
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        //为了实现点赞操作的原子性，对代码进行重构。
//        //根据实体类型和实体ID生成一个在Redis中用于存储点赞信息的键
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        //使用isMember()方法检查该集合中是否已经包含了该用户的ID
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember) {
//            //该用户已经点赞过，那么使用opsForSet().remove()方法从集合中移除该用户的ID。
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        }else {
//            //该用户尚未点赞过，那么使用opsForSet().add()方法将该用户的ID添加到集合中。
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        //利用事务机制保证了点赞操作的原子性，编程式事务处理
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //增加冗余参数entityUserId，避免了访问数据库来获取实体的用户ID，提高了执行效率。
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //使用opsForSet().isMember()方法检查该集合中是否已经包含了该用户的ID
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
                operations.multi();//开启事务
                if(isMember) {
                    //该用户已经点赞过，那么使用opsForSet().remove()方法从集合中移除该用户的ID。
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //被赞用户被赞数-1
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //该用户尚未点赞过，那么使用opsForSet().add()方法将该用户的ID添加到集合中。
                    operations.opsForSet().add(entityLikeKey, userId);
                    //被赞用户被赞数+1
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();//提交事务
            }
        });
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

    /**
     * 查询某个用户获得的赞
     */
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
