package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Duration;

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

    // 可以直接使用小时数进行比较
    @Transient  // 临时字段，存储小时数
    private Double watchHours;

    @Column(name = "uniqueVideos")
    private Integer uniqueVideos = 0;

}