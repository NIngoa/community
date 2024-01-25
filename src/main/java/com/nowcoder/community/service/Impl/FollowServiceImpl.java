package com.nowcoder.community.service.Impl;

import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
}
