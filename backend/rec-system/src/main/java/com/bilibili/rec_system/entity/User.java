package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    @JsonProperty("userId")
    private Long userId;

    @Column(name = "username", length = 100)
    @JsonProperty("username")
    private String username;

    @Column(name = "registerTime")
    @JsonProperty("registerTime")
    private LocalDateTime registerTime;

    @Column(name = "avatarPath", length = 255)
    @JsonProperty("avatarPath")
    private String avatarPath;
}