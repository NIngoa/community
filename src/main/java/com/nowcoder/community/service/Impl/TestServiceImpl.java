package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.TestService;
import com.nowcoder.community.utils.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;


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
        post.setCreateTime(LocalDateTime.now());
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
                post.setCreateTime(LocalDateTime.now());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("abc");
                return "ok";
            }
        });
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