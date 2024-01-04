package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService{

    List<DiscussPost> findDiscussPosts(Integer userId, int page, int pageSize);


    int findDiscussPostRows(int userId);
}
