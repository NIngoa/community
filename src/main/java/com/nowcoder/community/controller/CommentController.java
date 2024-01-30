package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    /**
     * 添加评论
     * @return
     */
    @RequestMapping("/add/{discussPostId}")
    public String addComment(@PathVariable int discussPostId, Comment comment) {
        User user = hostHolder.getUser();
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(LocalDateTime.now());
        commentService.insertComment(comment);
        //触发评论事件
        Event event=new Event();
        event.setTopic(TOPIC_COMMENT);
        event.setUserId(user.getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        event.setData("postId",discussPostId);
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost discussPost = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPost.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment commentById = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(commentById.getUserId());
        }
        eventProducer.produce(event);
        return "redirect:/discuss/detail/"+discussPostId;
    }
}
