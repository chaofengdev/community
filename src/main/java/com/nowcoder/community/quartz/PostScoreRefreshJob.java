package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {//实现两个接口

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);//记录日志

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 牛客纪元 静态常量
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    /**
     * Quartz调度任务，从redis中获取帖子ID，遍历这些ID，并使用refresh方法进行刷新
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 通过RedisTemplate获取与给定的redisKey相关联的Set类型数据的操作对象，即BoundSetOperations。
        // 通过这个操作对象，您可以对Redis中的Set类型数据进行各种操作。
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        // 如果Set为空，它会记录一条消息指示没有需要刷新的帖子，并从方法中返回。
        if(operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子");
            return ;
        }

        // 如果Set中有帖子，则继续进行刷新过程。它记录一条消息指示刷新过程已经开始。
        logger.info("[任务开始] 正在刷新帖子分数");
        // 使用while循环来迭代Set中的所有元素。循环会一直执行，直到Set为空为止。
        while(operations.size() > 0) {
            this.refresh((Integer) operations.pop());//获取的帖子ID，刷新帖子
        }
        // 循环完成后，该方法记录一条消息指示帖子分数已刷新。
        logger.info("[任务结束] 帖子分数刷新完毕");
    }

    public void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post == null) {
            logger.error("id = " + postId + " 的帖子不存在！");
            return;
        }

        // 是否是精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        // 分数 分数 = log(精华分 + 评论数 * 10 + 点赞数 * 2 + 收藏数) + (发布时间 - 牛客纪元)
        // getTime() 方法会将日期的时间值作为 long 类型数值返回。表示自1970年1月1日00:00:00 UTC（Unix纪元时间）以来的毫秒数。
        double score = Math.log10(Math.max(weight,1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }

}
