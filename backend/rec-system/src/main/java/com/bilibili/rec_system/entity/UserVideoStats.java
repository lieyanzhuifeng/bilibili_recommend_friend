package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_video_stats")
@Data
public class UserVideoStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statID")
    private Long statId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "watchCount")
    private Integer watchCount = 0;

    @Column(name = "totalWatchDuration")
    private Integer totalWatchDuration = 0;
}