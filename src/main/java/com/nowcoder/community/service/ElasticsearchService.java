package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {
    void saveDiscussPost(DiscussPost discussPost);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}
