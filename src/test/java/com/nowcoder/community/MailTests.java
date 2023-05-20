package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine; //通过templateEngine调用模板引擎，这样就不用再写controller了。

    @Test
    public void testTextMail() {
        //调用工具类中的方法sendMail，用客户端通过QQ服务器向130xxx发送邮件，邮件的主题是test，内容为普通文本welcome
        mailClient.sendMail("2353815549@qq.com","test e-mail3","welcome to my first e-mail...");
    }

    @Test
    public void testHtmlMail() {
        //内容对象
        Context context = new Context();
        context.setVariable("username","chaofeng");
        //将模板地址和相关内容对象传给模板引擎，返回字符串内容
        String content = templateEngine.process("/mail/demo",context);
        //控制台打印，本质上就是HTML网页内容
        System.out.println(content);
        //通过客户端，用服务器发送邮件
        mailClient.sendMail("2353815549@qq.com","test Html e-mail",content);
    }
}
