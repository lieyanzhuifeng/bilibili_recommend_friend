package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "user_top_themes")
@Data
public class UserTopTheme {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "themeID")
    private Long themeId;

    @Column(name = "proportion", precision = 5, scale = 4)
    private BigDecimal proportion;
}
