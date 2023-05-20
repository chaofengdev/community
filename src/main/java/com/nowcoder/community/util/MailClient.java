package com.nowcoder.community.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    //日志对象 注意这里导入的都是org.slf4j的包
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender javaMailSender;

    // 将application.properties中的变量spring.mail.username直接注入到from中
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件的核心逻辑
     * @param to
     * @param subject
     * @param context
     */
    public void sendMail(String to, String subject, String context) {
        //以下代码本质上都是利用导入的jar包或者说类库，源码都来自于spring-boot-starter-mail
        //消息对象
        MimeMessage message = javaMailSender.createMimeMessage();
        //帮助类
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            //利用帮助类设置相关信息
            helper.setFrom(from);//发件人
            helper.setTo(to);//收件人
            helper.setSubject(subject);//主题，可以理解为是标题
            helper.setText(context,true);//true表示开启xml文件支持，否则都当做一般文本处理
            javaMailSender.send(helper.getMimeMessage());//没有这句，邮箱不会发送相关信息，因为没有将message通过javaMailSender发送出去
        } catch (MessagingException e) {
            //利用日志打印失败信息，便于调试和测试
            logger.error("发送邮件失败" + e.getMessage());
        }
    }

}

