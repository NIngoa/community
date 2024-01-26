package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(Integer entityType, Integer entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJsonStr(0, "已关注");
    }

    /**
     * 取消关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(Integer entityType, Integer entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJsonStr(0, "已取消关注");
    }

    @RequestMapping(path = "/followee/{userId}", method = RequestMethod.GET)
    public String getFollowee(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followee/" + userId);
        Long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        page.setRows(followeeCount.intValue());

        List<Map<String, Object>> followeeList = followService.findFollowee(userId, page.getOffset(), page.getLimit());
        if (followeeList != null) {
            for (Map<String, Object> map : followeeList) {
                User u = (User) map.get("user");
                Boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followeeList", followeeList);
        return "/site/followee";
    }

    @RequestMapping(path = "/follower/{userId}", method = RequestMethod.GET)
    public String getFollower(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/follower/" + userId);
        Long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        page.setRows(followerCount.intValue());

        List<Map<String, Object>> followerList = followService.findFollower(userId, page.getOffset(), page.getLimit());
        if (followerList != null) {
            for (Map<String, Object> map : followerList) {
                User u = (User) map.get("user");
                Boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followerList", followerList);
        return "/site/follower";
    }

    private Boolean hasFollowed(Integer userId) {
        User user = hostHolder.getUser();
        if (user == null) {
            return false;
        }
        return followService.isFollow(user.getId(), ENTITY_TYPE_USER, userId);
    }
}
