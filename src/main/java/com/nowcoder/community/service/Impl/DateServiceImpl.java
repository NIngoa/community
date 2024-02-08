package com.nowcoder.community.service.Impl;

import com.nowcoder.community.service.DateService;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class DateServiceImpl implements DateService {
    @Autowired
    private RedisTemplate redisTemplate;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    //将指定的ip计入uv
    @Override
    public void recordUV(String ip) {
        String date = dateTimeFormatter.format(LocalDate.now());
        String uvKey = RedisUtil.getUVKey(date);
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    //统计指定范围内的UV
    @Override
    public long calculateUV(LocalDate start, LocalDate end) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //获取所有需要统计的key
        List<String> uvKeys = new ArrayList<>();

        LocalDate temStart = start;
        while (!temStart.isAfter(end)) {
            String uvKey = RedisUtil.getUVKey(dateTimeFormatter.format(temStart));
            uvKeys.add(uvKey);
            temStart=temStart.plusDays(1);
        }
        //合并数据
        String uvKey = RedisUtil.getUVKey(dateTimeFormatter.format(start), dateTimeFormatter.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey, uvKeys.toArray());
        //返回统计的结果
        Long size = redisTemplate.opsForHyperLogLog().size(uvKey);
        return size;
    }

    //记录指定用户的DAU
    public void recordDAU(int userId) {
        String date = dateTimeFormatter.format(LocalDate.now());
        String dauKey = RedisUtil.getDAUKey(date);
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    //统计指定范围内的DAU
    public long calculateDAU(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        List<byte[]>dauKeys=new ArrayList<>();

        LocalDate temStart = start;
        while (!temStart.isAfter(end)) {
            String dauKey = RedisUtil.getDAUKey(dateTimeFormatter.format(temStart));
            dauKeys.add(dauKey.getBytes());
           temStart=temStart.plusDays(1);
        }

        //进行OR运算
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String dauKey = RedisUtil.getDAUKey(dateTimeFormatter.format(start), dateTimeFormatter.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, dauKey.getBytes(), dauKeys.toArray(new byte[0][0]));
                return redisConnection.bitCount(dauKey.getBytes());
            }
        });
        return (long)execute;
    }

}
