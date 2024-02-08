package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 发布话题
     *
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonStr(403, "请先登录");
        }
        int userId = user.getId();
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(userId);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(userId)
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.produce(event);

        return CommunityUtil.getJsonStr(0, "发布成功");
    }

    /**
     * 详情
     *
     * @param discussPostId
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 获取帖子信息
        DiscussPost discussPost = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        // 获取用户信息
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        // 获取评论信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());
        // 获取一级评论列表
        List<Comment> commentsList = commentService.findCommentByEntity(ENTITY_TYPE_POST,
                discussPostId, page.getOffset(), page.getLimit());
        // 评论VO集合
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        if (commentsList != null) {
            for (Comment comment : commentsList) {
                //评论VO
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment", comment);
                //评论的用户
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                //回复列表
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                //回复列表VO
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                //添加回复列表所需的数据
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //回复的用户
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //被回复的用户
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                        replyVo.put("likeStatus", likeStatus);
                        //添加到回复VO列表
                        replyVoList.add(replyVo);
                    }
                }
                //将回复VO列表添加到评论VO列表
                commentVo.put("replyVo", replyVoList);
                //回复数量
                int count = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("count", count);
                commentVoList.add(commentVo);
            }
        }
        //添加到模版
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(Integer id) {
        discussPostService.updateType(id, DISCUSS_POST_TOP);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.produce(event);

        return CommunityUtil.getJsonStr(0, "置顶成功");
    }

    //加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(Integer id) {
        discussPostService.updateStatus(id, DISCUSS_POST_WONDERFUL);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.produce(event);
        return CommunityUtil.getJsonStr(0, "加精成功");
    }

    //删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(Integer id) {
        discussPostService.updateStatus(id, DISCUSS_POST_DELETE);
        //触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.produce(event);
        return CommunityUtil.getJsonStr(0, "删除成功");
    }
}
