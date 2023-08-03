package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;//编程式事务支持

    public AlphaService() {
//        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
//        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
//        System.out.println("销毁AlphaService");
    }

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    public String find() {
        return alphaDao.select();
    }


    /**
     * 事务基本概念。
     * =================================================================================================================
     * 事务相关：参考链接：https://www.bilibili.com/video/BV1R8411c7m2
     * 1.隔离级别：Read uncommitted 、Read committed、Repeatable read 、Serializable
     * 读未提交：读取了即将回滚的事务，产生脏读；
     * 读已提交：读取同一条数据，两次结果不一致，产生不可重复读。
     * 重复读：读取同一张表，两次表中记录条数不一致，产生幻读；
     * 串行化：事务顺序执行，可以避免脏读、不可重复度、幻读。
     * 上面只是凝练的表达，是根据场景总结提炼的文字，主要是区分读已提交和重复读的区别。
     *
     * =================================================================================================================
     * 2.传播机制；挂起、融入、嵌套
     * REQUIRED：支持当前事务，如果不存在，则创建新事务；--融入原来的事务
     * REQUIRES_NEW：创建一个新事务，并且暂停当前事务；--挂起原来的事务，开启新事务
     * NESTED：如果当前存在事务，则嵌套在该事务中执行，否则和REQUIRED一样。--嵌套，采用保存点方式回滚。
     * 传播机制有很多，但主要在于理解事务传播的三种基本情况，挂起、融入、嵌套。
     *
     * 3.spring对事务的支持
     * Spring可以支持编程式事务和声明式事务。下面save1是声明式事务支持，save2是编程式事务支持。
     * Spring提供的最原始的事务管理方式是基于TransactionDefinition、PlatformTransactionManager、TransactionStatus编程式事务。
     * 而TransactionTemplate的编程式事务管理是使用`模板方法设计模式`对原始事务管理方式的封装。
     * =================================================================================================================
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)//声明式事务支持
    public Object save1() {//声明式事务演示
        //新增用户
        User user = new User();
        user.setUsername("chaofeng1111");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("1304642992@qq.com");
        user.setHeaderUrl("https://images.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);//数据库主键自增且赋值给对象对应字段

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello...");
        post.setContent("新人报道，大家多多指教。");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        //NumberFormatException
        Integer.valueOf("abc");//这里将字符串转成整数，会报错。
        return "ok";
    }

    public Object save2() {//编程式事务演示
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {//匿名内部类 该类实现了TransactionCallback接口，并在doInTransaction方法中定义了具体的操作逻辑。
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("chaofeng2222");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("1304642992@qq.com");
                user.setHeaderUrl("https://images.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);//数据库主键自增且赋值给对象对应字段

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好~");
                post.setContent("新人报道，大家多多指教。");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                //NumberFormatException
                Integer.valueOf("abc");//这里将字符串转成整数，会报错。
                return "ok";
            }
        });
    }

    // 让该方法在多线程的环境下，被异步调用。
    // 使用了@Async注解将execute1()方法标记为异步方法，该方法会被异步调用，不会阻塞当前线程，用于执行"被异步调用。"的日志输出。
    // 异步方法通常用于在后台执行一些耗时的操作，以避免阻塞主线程。
    @Async
    public void execute1() {
        logger.debug("被异步调用。");
    }

    // 让该方法在多线程环境下，定时执行
    // 使用了@Scheduled注解将execute2()方法标记为定时任务，该方法会在应用启动后的10秒后开始执行，并每隔1秒执行一次，用于执行日志输出。
    // @Scheduled(initialDelay = 10000, fixedRate = 1000)//开始时间 执行时间间隔
    public void execute2() {
        logger.debug("被异步调用，定时执行。");
    }
}
