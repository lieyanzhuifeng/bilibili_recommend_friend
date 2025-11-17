package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_tag_stats")
@Data
public class UserTagStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagStatID")
    private Long tagStatId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "tagID")
    private Long tagId;

    @Column(name = "totalWatchDuration")
    private Integer totalWatchDuration = 0;

    @Column(name = "uniqueVideos")
    private Integer uniqueVideos = 0;
}