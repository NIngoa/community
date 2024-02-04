package com.nowcoder.community.service.Impl;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import com.nowcoder.community.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    //    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据id查询用户
     */
    @Override
    public User findUserById(int userId) {
//        return userMapper.selectById(userId);
        User user = getCache(userId);
        if (user == null) {
            user = initCache(userId);
        }
        return user;
    }


    @Override
    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisUtil.getTicketKey(ticket);
        Object o = redisTemplate.opsForValue().get(ticketKey);
        return CommunityUtil.getObj(o, LoginTicket.class);
    }

    /**
     * 注册
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //判断用户是否为空
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //判断用户名是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        //判断密码是否为空
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        //判断邮箱是否为空
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //判断用户名是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        //判断邮箱是否存在
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已注册！");
            return map;
        }
        //注册用户
        user.setUsername(user.getUsername());
        user.setSalt(CommunityUtil.getUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.getUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活邮件", content);
        return map;
    }

    /**
     * 激活
     */
    @Override
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        int status = user.getStatus();
        if (status == 1) {
            return ACTIVATION_REPEAT;
        } else if (status == 0 && user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            cleanCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAIL;
        }
    }

    /**
     * 登录
     */
    @Override
    public Map<String, Object> login(String username, String password, int expiredTime) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该用户不存在");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该用户未激活");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确");
            return map;
        }
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.getUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(LocalDateTime.now().plusSeconds(expiredTime));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisUtil.getTicketKey(ticket);
        Object o = redisTemplate.opsForValue().get(redisKey);
        LoginTicket loginTicket = CommunityUtil.getObj(o, LoginTicket.class);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }


    @Override
    public int updateHeader(int userId, String headUrl) {
        int rows = userMapper.updateHeader(userId, headUrl);
        cleanCache(userId);
        return rows;
    }

    @Override
    public void changePassword(String ticket, String newPassword, User user) {
        int userId = user.getId();
        userMapper.updatePassword(userId, CommunityUtil.md5(newPassword + user.getSalt()));
        cleanCache(userId);
        logout(ticket);
    }

    @Override
    public User findUserByUsername(String toName) {
        return userMapper.selectByName(toName);
    }

    //优先从缓存中获取用户信息
    @Override
    public User getCache(Integer userId) {
        String userKey = RedisUtil.getUserKey(userId);
        Object o = redisTemplate.opsForValue().get(userKey);
        return CommunityUtil.getObj(o, User.class);
    }

    @Override
    public User initCache(Integer userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }


    @Override
    public void cleanCache(Integer userId) {
        redisTemplate.delete(RedisUtil.getUserKey(userId));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                if (user.getType() == 1) {
                    return AUTHORITY_ADMIN;
                } else if (user.getType() == 2) {
                    return AUTHORITY_MODERATOR;
                } else {
                    return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
