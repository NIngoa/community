package com.nowcoder.community.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


//@Scope("prototype")
public interface TestService {


    public void testservice();


    @PostConstruct
    public void init();

    @PreDestroy
    public void destroy();

    public Object testTransaction();

    public Object save();

    public void execute1();

    public void execute2();
}
