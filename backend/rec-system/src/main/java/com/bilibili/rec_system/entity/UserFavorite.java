package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites")
@Data
public class UserFavorite {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "favoriteTime")
    private LocalDateTime favoriteTime;
}
