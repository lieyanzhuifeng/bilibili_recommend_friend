package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites")
@Data
@IdClass(UserFavorite.UserFavoriteId.class)
public class UserFavorite {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "videoID")
    private Long videoId;

    @Column(name = "favoriteTime")
    private LocalDateTime favoriteTime;

    // 使用Lombok自动生成equals和hashCode
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserFavoriteId implements java.io.Serializable {
        private Long userId;
        private Long videoId;
    }
}