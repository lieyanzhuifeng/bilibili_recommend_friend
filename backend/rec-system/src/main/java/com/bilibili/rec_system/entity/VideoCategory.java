package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "video_category")
@Data
public class VideoCategory {
    @Id
    @Column(name = "categoryID")  // 去掉 @GeneratedValue，因为categoryID是业务主键
    private Long categoryId;

    @Column(name = "categoryName", length = 100)
    private String categoryName;
}