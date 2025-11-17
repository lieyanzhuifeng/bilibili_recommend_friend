package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Data
public class Friend {
    @Column(name = "userID")
    private Long userId;

    @Column(name = "friendID")
    private Long friendId;

    @Column(name = "status", length = 8)
    private String status;

    @Column(name = "createTime")
    private LocalDateTime createTime;
}