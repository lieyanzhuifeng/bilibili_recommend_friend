package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "user_top_categories")
@Data
@IdClass(UserTopCategory.UserTopCategoryId.class)
public class UserTopCategory {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "categoryID")
    private Long categoryId;

    @Column(name = "proportion", precision = 5, scale = 4)
    private BigDecimal proportion;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserTopCategoryId implements java.io.Serializable {
        private Long userId;
        private Long categoryId;
    }
}