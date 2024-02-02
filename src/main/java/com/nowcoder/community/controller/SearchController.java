package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;

    // 搜索帖子
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        org.springframework.data.domain.Page<DiscussPost> discussPosts = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPosts==null?0:(int) discussPosts.getTotalElements());

        List<Map<String, Object>> discussPostList = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String,Object>map=new HashMap<>();
                map.put("post",discussPost);
                map.put("user",userService.findUserById(discussPost.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));
                discussPostList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPostList);
        model.addAttribute("keyword", keyword);

        return "/site/search";
    }

}
