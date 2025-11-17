package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "user_watch_log")
@Data
public class UserWatchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logID")
    private Long logId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "watchDate")
    private LocalDate watchDate;

    @Column(name = "watchDuration")
    private Integer watchDuration;
}