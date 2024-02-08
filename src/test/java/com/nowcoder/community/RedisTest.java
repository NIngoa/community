package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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
        redisTemplate.opsForValue().set("name", "狗屎");
        redisTemplate.opsForValue().set("age", 10);
        System.out.println(redisTemplate.opsForValue().get("name"));
    }

    @Test
    public void test2() {
        redisTemplate.opsForHash().put("username:1", "name", "花花");
        redisTemplate.opsForHash().put("username:2", "name", "嘎嘎");

        System.out.println(redisTemplate.opsForHash().get("username:1", "name"));
    }

    @Test
    public void test3() {
        redisTemplate.opsForList().leftPush("list:1", 1);
        redisTemplate.opsForList().leftPush("list:1", 2);
        redisTemplate.opsForList().leftPush("list:1", 3);
        System.out.println(redisTemplate.opsForList().size("list:1"));
        System.out.println(redisTemplate.opsForList().index("list:1", 0));
        System.out.println(redisTemplate.opsForList().range("list:1", 0, 2));

        for (int i = 0; i < 3; i++) {
            redisTemplate.opsForList().leftPop("list:1");
        }
    }

    @Test
    public void test4() {
        redisTemplate.opsForSet().add("set:1", 1, 2, 3, 4, 5, 6, 7, 8, 9);
        System.out.println(redisTemplate.opsForSet().size("set:1"));
        System.out.println(redisTemplate.opsForSet().members("set:1"));
        System.out.println(redisTemplate.opsForSet().pop("set:1"));
    }

    @Test
    public void test5() {
        redisTemplate.opsForZSet().add("zSet:1", 1, 1.0);
        redisTemplate.opsForZSet().add("zSet:1", 2, 2.0);
        redisTemplate.opsForZSet().add("zSet:1", 3, 3.0);
        System.out.println(redisTemplate.opsForZSet().zCard("zSet:1"));
        System.out.println(redisTemplate.opsForZSet().score("zSet:1", 1));
        System.out.println(redisTemplate.opsForZSet().rank("zSet:1", 1));
        System.out.println(redisTemplate.opsForZSet().reverseRank("zSet:1", 1));
        System.out.println(redisTemplate.opsForZSet().range("zSet:1", 0, 1));
        System.out.println(redisTemplate.opsForZSet().reverseRange("zSet:1", 0, 1));
    }

    @Test
    public void test6() {
        redisTemplate.delete("zSet:1");
        System.out.println(redisTemplate.hasKey("zSet:1"));
        redisTemplate.expire("zSet:1", 10, TimeUnit.SECONDS);
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
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForSet().add("set", "gaga");
                operations.opsForSet().add("set", "lala");
                operations.opsForSet().add("set", "huahua");
                System.out.println(operations.opsForSet().members("set"));
                return operations.exec();
            }
        });
        System.out.println(execute);
    }

    //统计二十万个重复数据的独立总数
    @Test
    public void testHyperLog() {
        String redisKey = "test:hll:01";
        for (int i = 0; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 0; i < 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    //将三组数据合并，在统计合并后的重复数据的独立总数
    @Test
    public void testMerge() {
        String redisKey2 = "test:hll:02";
        String redisKey3 = "test:hll:03";
        String redisKey4 = "test:hll:04";
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        for (int i = 5001; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        for (int i = 10001; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String mergeKey = "test:hll:merge";
        redisTemplate.opsForHyperLogLog().union(mergeKey, redisKey2, redisKey3, redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(mergeKey);
        System.out.println(size);
        ;
    }

    @Test
    public void testBitMap() {
        String redisKey = "test:bitmap:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 10, true);
        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        //统计
        Object execute = redisTemplate.execute(new RedisCallback<>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(execute);
    }

    //统计三组数据的布尔值，并对这三组数据做or运算
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bitmap:02";

        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bitmap:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bitmap:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String mergeKey = "test:bitmap:merge";
        Object execute = redisTemplate.execute(new RedisCallback<>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        mergeKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return redisConnection.bitCount(mergeKey.getBytes());
            }
        });
        System.out.println(execute);

        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(mergeKey, 6));
    }
}
