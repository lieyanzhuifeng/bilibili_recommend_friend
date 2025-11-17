package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_up_stats")
@Data
public class UserUpStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upStatID")
    private Long upStatId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "upID")
    private Long upId;

    @Column(name = "totalWatchDuration")
    private Integer totalWatchDuration = 0;

    @Column(name = "uniqueVideos")
    private Integer uniqueVideos = 0;
}