package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentID")
    private Long commentId;

    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "parentID")
    private Long parentId = 0L;
}