package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;

import java.util.Map;

public interface UserService {


    User findUserById(int userId);

    LoginTicket findLoginTicket(String ticket);

    Map<String, Object> register(User user);

    int activation(int userId, String code);

    Map<String, Object> login(String username, String password, int expiredTime);

    void logout(String ticket);

    int updateHeader(int userId,String headUrl);

    void changePassword(String ticket,String newPassword, User user);

    User findUserByUsername(String toName);
}
