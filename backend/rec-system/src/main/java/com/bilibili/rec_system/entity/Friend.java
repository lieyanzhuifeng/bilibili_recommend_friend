package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Data
@IdClass(Friend.FriendId.class)
public class Friend {
    @Id
    @Column(name = "userID")
    private Long userId;

    @Id
    @Column(name = "friendID")
    private Long friendId;

    @Column(name = "status", length = 8)
    private String status;

    @Column(name = "createTime")
    private LocalDateTime createTime;

    // 使用Lombok自动生成equals和hashCode
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FriendId implements java.io.Serializable {
        private Long userId;
        private Long friendId;
    }
}