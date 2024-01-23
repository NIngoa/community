package com.nowcoder.community.service.Impl;

import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    public void like(int userId, Integer entityType, Integer entityId) {
        String likeKey = RedisUtil.getEntityLikeKey(entityId, entityType);
        Boolean member = redisTemplate.opsForSet().isMember(likeKey, userId);
        if (member) {
            redisTemplate.opsForSet().remove(likeKey, userId);
        } else {
            redisTemplate.opsForSet().add(likeKey, userId);
        }
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

    @Override
    public Integer findEntityLikeStatus(int userId, Integer entityType, Integer entityId) {
        String likeKey = RedisUtil.getEntityLikeKey(entityId, entityType);
        Boolean member = redisTemplate.opsForSet().isMember(likeKey, userId);
        return member ?1:0;
    }
}
