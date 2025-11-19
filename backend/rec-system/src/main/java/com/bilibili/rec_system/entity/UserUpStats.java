package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Duration;

@Entity
@Table(name = "user_up_stats")
@Data
public class UserUpStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upStatID")
    private Long upStatId;

    @Column(name = "userID")
    private Long userId;

    @Column(name = "upID")
    private Long upId;

    @Transient  // 标记为不持久化到数据库
    private Duration totalWatchDuration = Duration.ZERO;

    @Column(name = "uniqueVideos")
    private Integer uniqueVideos = 0;

    // 便捷方法：获取格式化后的时长字符串
    public String getFormattedDuration() {
        if (totalWatchDuration == null) {
            return "00:00:00";
        }

        long seconds = totalWatchDuration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}