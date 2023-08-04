package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;


//配置-》数据库-》调用
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程
    // 1.利用`@Bean`将`FactoryBean`注入到Spring容器中时，Spring会自动获得`FactoryBean`产生的实例化对象。
    // 2.将`FactoryBean`定义为`@Bean`时，实际上注册的是`FactoryBean`的实例，而不是`FactoryBean`本身。
    // Spring在获取这个Bean时，会调用`FactoryBean`的`getObject()`方法，从而获取`FactoryBean`所生产的实例化对象。

    /**
     * 配置JobDetail
     * 创建并配置一个JobDetail的Bean，并将其注册到Spring容器中。
     * @return
     */
    // @Bean //默认使用方法名作为Bean的名称，除非使用name属性指定，如：@Bean(name = "customBeanName")
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 设置JobDetail关联的作业类，即将要执行的具体任务类。
        factoryBean.setJobClass(AlphaJob.class);
        // 设置作业的名称
        factoryBean.setName("alphaJob");
        // 设置作业的组名
        factoryBean.setGroup("alphaJobGroup");
        // 设置作业的持久性，意味着即使没有触发器与其关联，该作业也会保留在Quartz的作业存储中。
        factoryBean.setDurability(true);
        // 设置作业是否支持恢复（Recovery）。如果设置为true，当应用程序重新启动时，如果作业执行失败，Quartz将尝试重新执行失败的作业。
        factoryBean.setRequestsRecovery(true);
        // 返回一个正确配置的JobDetailFactoryBean实例，该实例可以在Spring容器中注册为一个Bean，并用于定义作业的属性和行为。
        // 将该JobDetail与触发器（Trigger）相关联，从而在指定的时间点或时间间隔执行您的AlphaJob任务。
        return factoryBean;
    }

    /**
     * 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
     * 创建并配置一个SimpleTrigger的Bean，并将其注册到Spring容器中。
     * SimpleTrigger是Quartz框架中最简单的触发器类型，可以用于在指定的时间间隔或指定的时间点触发任务执行。
     * @param alphaJobDetail
     * @return
     */
    // @Bean
    public SimpleTriggerFactoryBean simpleTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 将之前定义的alphaJobDetail作业关联到这个触发器，表示该触发器要触发执行alphaJobDetail所描述的任务。
        factoryBean.setJobDetail(alphaJobDetail);
        // 设置触发器的名称
        factoryBean.setName("alphaTrigger");
        // 设置触发器的组名
        factoryBean.setGroup("alphaTriggerGroup");
        // 设置触发器的重复间隔，这里设置为1000毫秒（1秒）。意味着任务将每隔1秒执行一次。
        factoryBean.setRepeatInterval(1000);
        // 设置触发器的任务数据Map。JobDataMap是Quartz中传递参数给任务的一种方式，这里创建一个空的JobDataMap。
        factoryBean.setJobDataMap(new JobDataMap());
        // 返回一个正确配置的SimpleTriggerFactoryBean实例，该实例可以在Spring容器中注册为一个Bean，并用于定义触发器的属性和行为。
        // 将这个触发器与之前定义的作业alphaJobDetail关联后，AlphaJob任务将会在每隔1秒执行一次。
        return factoryBean;
    }


    // 刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 设置JobDetail关联的作业类，即将要执行的具体任务类。
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        // 设置作业的名称
        factoryBean.setName("postScoreRefreshJob");
        // 设置作业的组名
        factoryBean.setGroup("communityJobGroup");
        // 设置作业的持久性，意味着即使没有触发器与其关联，该作业也会保留在Quartz的作业存储中。
        factoryBean.setDurability(true);
        // 设置作业是否支持恢复（Recovery）。如果设置为true，当应用程序重新启动时，如果作业执行失败，Quartz将尝试重新执行失败的作业。
        factoryBean.setRequestsRecovery(true);
        // 返回一个正确配置的JobDetailFactoryBean实例，该实例可以在Spring容器中注册为一个Bean，并用于定义作业的属性和行为。
        // 将该JobDetail与触发器（Trigger）相关联，从而在指定的时间点或时间间隔执行您的AlphaJob任务。
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 将之前定义的alphaJobDetail作业关联到这个触发器，表示该触发器要触发执行alphaJobDetail所描述的任务。
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        // 设置触发器的名称
        factoryBean.setName("postScoreRefreshTrigger");
        // 设置触发器的组名
        factoryBean.setGroup("communityTriggerGroup");
        // 设置触发器的重复间隔，这里设置为（1分钟）。意味着任务将每隔1秒执行一次。
        factoryBean.setRepeatInterval(1000 * 60);
        // 设置触发器的任务数据Map。JobDataMap是Quartz中传递参数给任务的一种方式，这里创建一个空的JobDataMap。
        factoryBean.setJobDataMap(new JobDataMap());
        // 返回一个正确配置的SimpleTriggerFactoryBean实例，该实例可以在Spring容器中注册为一个Bean，并用于定义触发器的属性和行为。
        // 将这个触发器与之前定义的作业alphaJobDetail关联后，AlphaJob任务将会在每隔1秒执行一次。
        return factoryBean;
    }

}
