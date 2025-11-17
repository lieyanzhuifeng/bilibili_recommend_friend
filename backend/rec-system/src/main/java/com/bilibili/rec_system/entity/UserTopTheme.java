package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "user_top_themes")
@Data
@IdClass(UserTopTheme.UserTopThemeId.class)
public class UserTopTheme {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "themeID")
    private Long themeId;

    @Column(name = "proportion", precision = 5, scale = 4)
    private BigDecimal proportion;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserTopThemeId implements java.io.Serializable {
        private Long userId;
        private Long themeId;
    }
}