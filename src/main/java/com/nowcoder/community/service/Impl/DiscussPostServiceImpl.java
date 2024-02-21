package com.nowcoder.community.service.Impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.utils.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    private static final Logger logger= LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口:Cache,LoadingCache,AsyncLoadingCache

    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>>postListCache;

    //帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        //初始化帖子列表缓存
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] split = key.split(":");
                        if (split==null||split.length!=2){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        Integer offset = Integer.valueOf(split[0]);
                        Integer limit = Integer.valueOf(split[1]);
                        //二级缓存:redis-->mysql
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache= Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }
    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, int offset, int limit, int orderMode) {
        if (userId==0 &&orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB.");
        List<DiscussPost> discussPostsList = discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
        return discussPostsList;
    }

    @Override
    public int findDiscussPostRows(int userId) {
        if (userId==0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        int rows = discussPostMapper.selectDiscussPostRows(userId);
        return rows;
    }

    public void addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new NullPointerException("参数不能为空!");
        }
        //转义html标签
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    @Override
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    @Override
    public Integer updateScore(int postId, double score) {
        Integer updateScore = discussPostMapper.updateScore(postId, score);
        return updateScore;
    }

    @Override
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }
}
