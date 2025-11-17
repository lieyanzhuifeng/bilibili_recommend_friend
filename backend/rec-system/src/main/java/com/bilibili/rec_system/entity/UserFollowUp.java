package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "user_follow_up")
@Data
@IdClass(UserFollowUp.UserFollowUpId.class)
public class UserFollowUp {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "upID")
    private Long upId;

    @Column(name = "followTime")
    private LocalDate followTime;

    // 使用Lombok自动生成equals和hashCode
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserFollowUpId implements java.io.Serializable {
        private Long userId;
        private Long upId;
    }
}