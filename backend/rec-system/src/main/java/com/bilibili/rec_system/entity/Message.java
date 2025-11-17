package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "messageID")
    private Long messageId;

    @Column(name = "senderID")
    private Long senderId;

    @Column(name = "receiverID")
    private Long receiverId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sendTime")
    private LocalDateTime sendTime;
}