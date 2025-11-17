package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "video_theme")
@Data
public class VideoTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "themeID")
    private Long themeId;

    @Column(name = "themeName", length = 100)
    private String themeName;
}