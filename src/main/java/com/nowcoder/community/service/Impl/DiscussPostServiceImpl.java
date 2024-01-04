package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, int page, int pageSize) {
        List<DiscussPost> discussPostsList = discussPostMapper.selectDiscussPosts(userId, page, pageSize);
        return discussPostsList;
    }

    @Override
    public int findDiscussPostRows(int userId) {
        int rows = discussPostMapper.selectDiscussPostRows(userId);
        return rows;
    }


}
