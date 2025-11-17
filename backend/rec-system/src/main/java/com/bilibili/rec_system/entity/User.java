package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "registerTime")
    private LocalDateTime registerTime;

    @Column(name = "avatarPath", length = 255)
    private String avatarPath;
}