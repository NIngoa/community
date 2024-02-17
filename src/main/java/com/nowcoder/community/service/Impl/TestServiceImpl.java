package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.TestService;
import com.nowcoder.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final Logger logger= LoggerFactory.getLogger(TestServiceImpl.class);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object testTransaction() {
        //新增用户
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setEmail("");
        user.setSalt(CommunityUtil.getUUID());
        user.setHeaderUrl("");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(user);
        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(1);
        post.setTitle("测试");
        post.setContent("测试");
        post.setType(1);
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");
        return "ok";
    }

    @Override
    public Object save() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("test");
                user.setPassword("123456");
                user.setEmail("");
                user.setSalt(CommunityUtil.getUUID());
                user.setHeaderUrl("");
                user.setCreateTime(LocalDateTime.now());
                userMapper.insertUser(user);
                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(1);
                post.setTitle("测试");
                post.setContent("测试");
                post.setType(1);
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("abc");
                return "ok";
            }
        });
    }

    @Async
    @Override
    public void execute1() {
        logger.debug("execute1");
    }

//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
    @Override
    public void execute2() {
        logger.debug("execute2");
    }

    @Override
    public void testservice() {
        System.out.println("实例化Service");
    }

    @Override
    public void init() {
        System.out.println("初始化Service");
    }

    @Override
    public void destroy() {
        System.out.println("销毁");
    }
}
