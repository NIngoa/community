package com.nowcoder.community;

import com.nowcoder.community.dao.Dao;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    @Autowired
    private UserMapper userMapper;

    private ApplicationContext applicationContext;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);

        Dao bean = applicationContext.getBean(Dao.class);
        System.out.println(bean.select());
        bean = applicationContext.getBean("mybatisBean", Dao.class);
        System.out.println(bean.select());
    }
    @Test
    public void testBeaManagement(){
        TestService bean = applicationContext.getBean(TestService.class);
        System.out.println(bean);
        bean = applicationContext.getBean(TestService.class);
        System.out.println(bean);
    }
    @Test
    public void simpleTest(){
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired
    @Qualifier("mybatisBean")
    private Dao dao;
    @Autowired
    private SimpleDateFormat simpleDateFormat;
    @Autowired
    private TestService testService;
    @Test
    public  void testDI(){
        System.out.println(dao);
        System.out.println(simpleDateFormat);
        System.out.println(testService);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setEmail("");
        user.setHeaderUrl("http://www.nowcoder.com/images/default_header.gif");
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(1);
        user.setType(1);
        user.setActivationCode("123456");
        user.setSalt("123456");
        int row= userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user);
    }
    @Test
    public void testloginticket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("d3f4e8b7f25547519f5b17627a6f423b");
        System.out.println(loginTicket);

    }
}
