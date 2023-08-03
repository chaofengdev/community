package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// 当一个类被@RunWith注解标记，JUnit会使用该注解引用的类作为测试运行器
// 测试运行器是指JUnit测试框架中负责执行测试用例的组件，JUnit提供了默认的测试运行器来执行测试类中的测试方法；
// 我们可以自定义测试运行器，实现特定的测试行为，这里我们定义SpringRunner作为测试运行器，用于在测试中使用Spring框架中的功能；
// SpringRunner是SpringJUnit4ClassRunner的别名。
@RunWith(SpringRunner.class)
// Spring Boot的测试注解，用来表示该类是一个Spring Boot应用程序的测试类；
// 会在测试开始时，自动启用Spring Boot应用程序，创建上下文环境，包括加载所有的bean和组件。
@SpringBootTest
// Spring测试注解的一部分，用于指定要加载的Spring配置类。
// CommunityApplication.class类通常包括了应用程序中的配置和组件信息。
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);//通过Executor工厂类创建不同类型的线程池，这里是使用FixedThreadPool创建一个固定大小的线程池。

    // JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);//同上，通过ScheduledThreadPool创建一个定时任务执行的线程池，执行延迟任务或者定期任务。


    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private AlphaService alphaService;


    private void sleep(long m) {//毫秒
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // JDK普通线程池
    @Test
    public void testExecutorService() {
        Runnable task = new Runnable() {//匿名内部类实现
            @Override
            public void run() {
                logger.debug("Hello, ExecutorService!");
            }
        };

        for (int i = 0; i < 10; i++) {
            //线程池管理一组线程，根据可用的线程来执行提交的任务
            //犹豫循环执行了10次submit()，每次提交的task会被分配到线程池中的一个空闲线程去执行
            executorService.submit(task);
        }

        //在springboot的测试方法中，执行完毕后，会关闭进程，不会等待各个线程运行结束，所以这里需要使用sleep
        sleep(10000);
    }

    // JDK定时任务线程池
    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello,ScheduledExecutorService!");
            }
        };

        //scheduleAtFixedRate方法会以固定频率执行任务，无论前一个任务是否完成，都会继续执行下一个任务。
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);//在固定的时间间隔内重复执行任务的方法

        sleep(30000);
    }


    // Spring线程池 -- 相对于jdk自带的线程池，这里可以在配置文件中指定线程池核心线程数量、扩展线程数量、任务队列大小、线程池调度线程数量
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello,ThreadPoolTaskExecutor!");
            }
        };

        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(task);
        }

        sleep(10000);
    }

    // Spring可执行定时任务的线程池
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello,ThreadPoolTaskScheduler!");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task, startTime, 1000);

        sleep(30000);
    }

    // Spring普通线程池（简化）
    @Test
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }
    }

    // Spring定时任务线程池（简化）
    @Test
    public void testThreadPoolTaskSchedulerSimple() {
        //alphaService.execute2();
        sleep(30000);//不需要手动调用，会被自动调用
    }


}
