package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;

import java.util.Map;

public interface UserService {


    User findUserById(int userId);

    Map<String,Object> register(User user);

    int activation(int userId,String code);

    Map<String,Object> login(String username,String password,int expiredTime);

    void logout(String ticket);
}
