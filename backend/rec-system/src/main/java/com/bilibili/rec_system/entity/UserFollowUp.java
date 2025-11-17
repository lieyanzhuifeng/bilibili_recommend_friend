package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "user_follow_up")
@Data
public class UserFollowUp {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "upID")
    private Long upId;

    @Column(name = "followTime")
    private LocalDate followTime;
}
