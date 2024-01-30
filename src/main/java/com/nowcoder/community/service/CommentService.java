package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment>findCommentByEntity(int entityType, int entityId, int offset, int limit);
    int findCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment findCommentById(int id);
}
