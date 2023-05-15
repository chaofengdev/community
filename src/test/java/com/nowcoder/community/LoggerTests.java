package com.nowcoder.community;



import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {

    private static final Logger logger =  LoggerFactory.getLogger(LoggerTests.class);

    /**
     *  测试日志级别，对于日志的相关知识点还需要深入了解，Logback是在log4j基础上重新开发的一套日志框架，是springboot默认使用的日志框架
     *  目前常用的Java日志框架：Logback、log4j、log4j2、JUL等。
     *  下面是Logback的B站教程：
     *  https://www.bilibili.com/video/BV1J3411e7oy/?spm_id_from=333.337.search-card.all.click&vd_source=7a968b6926ba162f674a367ff6d4a79b
     *
     */
    @Test
    public void testLogger() {
        System.out.println(logger.getName());//获取当前logger对象的名字

        /*
        * logger.debug的用处：
        * 简单来讲，就是配合log等级来过滤输出。
        * 根据log4j的配置等级，具体等级配置在springboot的配置文件application.properties中，logger记录日志分别对相应等级的内容进行输出
        * 比如，在开发时要验证一个方法有没有被调用，为了方便调试，通常会在方法开始时添加一些System.out，但是项目真正发布时这些代码需要移除，所以更建议使用logger来记录。
        * 当配置日志级别为warn，就只会显示warn以上的输出，即warn/error，debug/info不会显示。
        * */
        //生成一条日志，可以理解为默认配置下，System.out文本到控制台，便于观察和理解
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }
}
