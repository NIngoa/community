package com.nowcoder.community;

import com.nowcoder.community.service.TestService;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    //JDK线程池
    private ExecutorService executorService= Executors.newFixedThreadPool(5);
    //jdk定时执行任务的线程池
    private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(5);

    //Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    //Spring定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private TestService testService;

    private void sleep(int m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void ExecutorService() {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
               logger.debug("线程池执行任务");
            }
        };
        for (int i = 0; i < 10; i++) {
            executorService.submit(runnable);
        }
        sleep(1000);
}

    @Test
    public void scheduledExecutorService() {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                logger.debug("定时任务线程池执行任务");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnable,10000,1000, TimeUnit.MILLISECONDS);

        sleep(300000);
    }

    @Test
    public void threadPoolTaskExecutor() {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                logger.debug("线程池执行任务");
            }
        };
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(runnable);
        }
        sleep(10000);
    }

    @Test
    public void threadPoolTaskScheduler() {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                logger.debug("定时任务线程池执行任务");
            }
        };
        Date startTime=new Date(System.currentTimeMillis()+10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(runnable,startTime,1000);

        sleep(30000);
    }

    //5.Spring普通线程池（简化）
    @Test
    public void test(){
        for (int i = 0; i < 10; i++) {
            testService.execute1();
        }
        sleep(10000);
    }
    //6.Spring定时任务线程池（简化）
    @Test
    public void test2(){
        sleep(30000);
    }
}
