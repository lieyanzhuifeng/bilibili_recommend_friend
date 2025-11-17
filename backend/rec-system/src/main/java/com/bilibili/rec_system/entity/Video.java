package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "videos")
@Data
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "uploaderID")
    private Long uploaderId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "categoryID")
    private Long categoryId;

    @Column(name = "themeID")
    private Long themeId;

    @Column(name = "publishTime")
    private LocalDateTime publishTime;

    @Column(name = "duration")
    private LocalTime duration;
}