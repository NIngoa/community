package com.nowcoder.community;

import com.nowcoder.community.utils.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class mailTest {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void test() {
        String to = "1534868562@qq.com";
        String subject = "测试邮件";
        String content = "这是一封测试邮件";
        mailClient.sendMail(to, subject, content);
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "张三");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("1534868562@qq.com", "HTML", content);
    }
}
