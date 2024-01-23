package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void test() {
        redisTemplate.opsForValue().set("name","狗屎");
        redisTemplate.opsForValue().set("age",10);
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
    @Test
    public void test2() {
        redisTemplate.opsForHash().put("username:1","name","花花");
        redisTemplate.opsForHash().put("username:2","name","嘎嘎");

        System.out.println(redisTemplate.opsForHash().get("username:1","name"));
    }

    @Test
    public void test3() {
        redisTemplate.opsForList().leftPush("list:1",1);
        redisTemplate.opsForList().leftPush("list:1",2);
        redisTemplate.opsForList().leftPush("list:1",3);
        System.out.println(redisTemplate.opsForList().size("list:1"));
        System.out.println(redisTemplate.opsForList().index("list:1",0));
        System.out.println(redisTemplate.opsForList().range("list:1",0,2));

        for (int i = 0; i < 3; i++) {
            redisTemplate.opsForList().leftPop("list:1");
        }
    }

    @Test
    public void test4() {
        redisTemplate.opsForSet().add("set:1",1,2,3,4,5,6,7,8,9);
        System.out.println(redisTemplate.opsForSet().size("set:1"));
        System.out.println(redisTemplate.opsForSet().members("set:1"));
        System.out.println(redisTemplate.opsForSet().pop("set:1"));
    }
    @Test
    public void test5() {
        redisTemplate.opsForZSet().add("zSet:1",1,1.0);
        redisTemplate.opsForZSet().add("zSet:1",2,2.0);
        redisTemplate.opsForZSet().add("zSet:1",3,3.0);
        System.out.println(redisTemplate.opsForZSet().zCard("zSet:1"));
        System.out.println(redisTemplate.opsForZSet().score("zSet:1",1));
        System.out.println(redisTemplate.opsForZSet().rank("zSet:1",1));
        System.out.println(redisTemplate.opsForZSet().reverseRank("zSet:1",1));
        System.out.println(redisTemplate.opsForZSet().range("zSet:1",0,1));
        System.out.println(redisTemplate.opsForZSet().reverseRange("zSet:1",0,1));
    }

    @Test
    public void test6() {
        redisTemplate.delete("zSet:1");
        System.out.println(redisTemplate.hasKey("zSet:1"));
        redisTemplate.expire("zSet:1",10, TimeUnit.SECONDS);
    }

    @Test
    public void test7() {
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps("age");
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }
    @Test
    public void test8() {
        Object execute = redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForSet().add("set","gaga");
                operations.opsForSet().add("set","lala");
                operations.opsForSet().add("set","huahua");
                System.out.println(operations.opsForSet().members("set"));
                return operations.exec();
            }
        });
        System.out.println(execute);
    }
}
