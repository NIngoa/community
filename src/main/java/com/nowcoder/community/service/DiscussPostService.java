package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService{

    List<DiscussPost> findDiscussPosts(Integer userId, int offset, int limit,int orderMode);


    int findDiscussPostRows(int userId);

    void addDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

   Integer updateScore(int postId, double score);
}
