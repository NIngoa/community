package com.nowcoder.community.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class testService {

    public testService() {
        System.out.println("实例化Service");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化对象");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁对象");
    }

}
