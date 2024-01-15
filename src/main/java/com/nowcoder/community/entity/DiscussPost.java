package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DiscussPost {
    private int id;
    private int userId;
    // 标题
    private String title;
    // 文章内容
    private String content;
    private int type;
    private int status;
    private LocalDateTime createTime;
    // 评论数
    private int commentCount;
    // 点赞数
    private double score;
}
