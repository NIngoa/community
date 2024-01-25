package com.nowcoder.community.service.Impl;

import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    @Override
    public void like(int userId, Integer entityType, Integer entityId,Integer likeUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityId, entityType);
                String userLikeKey = RedisUtil.getUserLikeKey(likeUserId);
                Boolean member = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();
                if (member) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * 查询点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public long findEntityLikeCount(Integer entityType, Integer entityId) {
        String likeKey = RedisUtil.getEntityLikeKey(entityId, entityType);
         return redisTemplate.opsForSet().size(likeKey);
    }

    /**
     * 查询用户是否点赞
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public Integer findEntityLikeStatus(int userId, Integer entityType, Integer entityId) {
        String likeKey = RedisUtil.getEntityLikeKey(entityId, entityType);
        Boolean member = redisTemplate.opsForSet().isMember(likeKey, userId);
        return member ?1:0;
    }

    /**
     * 查询用户点赞数量
     * @param userId
     * @return
     */
    @Override
    public int selectLikeCount(int userId) {
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count= (Integer) redisTemplate.opsForValue().get(userLikeKey);
        if (count!=null) {
            return count;
        }
        return 0;
    }
}
