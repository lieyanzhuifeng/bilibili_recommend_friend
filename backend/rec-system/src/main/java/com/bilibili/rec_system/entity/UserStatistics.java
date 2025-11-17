package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "user_statistics")
@Data
public class UserStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statID")
    private Long statId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "totalWatchHours", precision = 10, scale = 2)
    private BigDecimal totalWatchHours = BigDecimal.ZERO;

    @Column(name = "likeRate", precision = 5, scale = 4)
    private BigDecimal likeRate = BigDecimal.ZERO;

    @Column(name = "coinRate", precision = 5, scale = 4)
    private BigDecimal coinRate = BigDecimal.ZERO;

    @Column(name = "favoriteRate", precision = 5, scale = 4)
    private BigDecimal favoriteRate = BigDecimal.ZERO;

    @Column(name = "shareRate", precision = 5, scale = 4)
    private BigDecimal shareRate = BigDecimal.ZERO;

    @Column(name = "activeDays")
    private Integer activeDays = 0;

    @Column(name = "nightWatchMinutes")
    private Integer nightWatchMinutes = 0;

    @Column(name = "mainCategoryID")
    private Long mainCategoryId;

    @Column(name = "mainUPID")
    private Long mainUpId;
}