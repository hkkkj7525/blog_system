package com.example.blog_system.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @Classname MailUtils
 * @Description 邮件发送工具类
 * @Created by CrazyStone
 */
@Component
public class MailUtils {
    private final JavaMailSender mailSender;
    private final String mailFrom;

    // 使用构造器注入
    @Autowired
    public MailUtils(JavaMailSender mailSender, @Value("${spring.mail.username}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    // 发送简单邮件
    public void sendSimpleEmail(String mailto, String title, String content) {
        // 定制邮件发送内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(mailto);
        message.setSubject(title);
        message.setText(content);
        // 发送邮件
        mailSender.send(message);
    }
}