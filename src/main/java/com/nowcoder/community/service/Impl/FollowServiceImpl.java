package com.nowcoder.community.service.Impl;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;
    @Override
    public void follow(Integer userId, Integer entityType, Integer entityId) {

        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(Integer userId, Integer entityType, Integer entityId) {

        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    /**
     * 查询关注数量
     * @param userId
     * @param entityType
     * @return
     */
    @Override
    public Long findFolloweeCount(Integer userId, Integer entityType) {
        return redisTemplate.opsForZSet().zCard(RedisUtil.getFolloweeKey(userId, entityType));
    }
    /**
     * 查询粉丝数量
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public Long findFollowerCount(Integer entityType, Integer entityId) {
        return redisTemplate.opsForZSet().zCard(RedisUtil.getFollowerKey(entityId,entityType));
    }


    @Override
    public Boolean isFollow(Integer userId, Integer entityType, Integer entityId) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    @Override
    public List<Map<String, Object>> findFollowee(int userId, int offset, int limit) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Object> set = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (set == null){
            return null;
        }
        List<Map<String, Object>>followeeList=new ArrayList<>();
        for (Object entityId : set) {
            Map<String,Object>map=new HashMap<>();
            User user = userService.findUserById((Integer) entityId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
            map.put("followTime", LocalDateTime.ofInstant(Instant.ofEpochMilli(score.longValue()), ZoneId.systemDefault()));
            followeeList.add(map);
        }
        return followeeList;
    }

    @Override
    public List<Map<String, Object>> findFollower(int userId, int offset, int limit) {
        String followerKey = RedisUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<Object> set = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (set == null){
            return null;
        }
        List<Map<String, Object>>followerList=new ArrayList<>();
        for (Object entityId : set) {
            Map<String,Object>map=new HashMap<>();
            User user = userService.findUserById((Integer) entityId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, entityId);
            map.put("followTime",LocalDateTime.ofInstant(Instant.ofEpochMilli(score.longValue()), ZoneId.systemDefault()));
            followerList.add(map);
        }
        return followerList;
    }
}
