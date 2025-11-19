package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.repository.UserUpStatsRepository;
import com.bilibili.rec_system.repository.UserUpStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserUpStatsService {

    private final UserUpStatsRepository userUpStatsRepository;

    /**
     * 获取所有用户UP主统计数据
     */
    public List<UserUpStats> findAll() {
        List<UserUpStatsProjection> projections = userUpStatsRepository.findAllWithFormattedDuration();
        return projections.stream()
                .map(this::mapToUserUpStats)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID查找
     */
    public List<UserUpStats> findByUserId(Long userId) {
        List<UserUpStatsProjection> projections = userUpStatsRepository.findByUserIdWithFormattedDuration(userId);
        return projections.stream()
                .map(this::mapToUserUpStats)
                .collect(Collectors.toList());
    }

    /**
     * 根据UP主ID查找
     */
    public List<UserUpStats> findByUpId(Long upId) {
        List<UserUpStatsProjection> projections = userUpStatsRepository.findByUpIdWithFormattedDuration(upId);
        return projections.stream()
                .map(this::mapToUserUpStats)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID和UP主ID查找
     */
    public Optional<UserUpStats> findByUserIdAndUpId(Long userId, Long upId) {
        Optional<UserUpStatsProjection> projection = userUpStatsRepository.findByUserIdAndUpIdWithFormattedDuration(userId, upId);
        return projection.map(this::mapToUserUpStats);
    }

    /**
     * 将投影转换为 UserUpStats 对象
     */
    private UserUpStats mapToUserUpStats(UserUpStatsProjection projection) {
        UserUpStats stats = new UserUpStats();

        stats.setUpStatId(projection.getUpStatId());
        stats.setUserId(projection.getUserId());
        stats.setUpId(projection.getUpId());
        stats.setTotalWatchDuration(parseDuration(projection.getTotalWatchStr()));
        stats.setUniqueVideos(projection.getUniqueVideos());

        return stats;
    }

    /**
     * 将 HH:mm:ss 格式的字符串转换为 Duration
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ZERO;
        }

        try {
            String[] parts = durationStr.split(":");
            if (parts.length == 3) {
                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                long seconds = Long.parseLong(parts[2]);

                return Duration.ofHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds(seconds);
            }
        } catch (NumberFormatException e) {
            System.err.println("解析时长失败: " + durationStr);
        }
        return Duration.ZERO;
    }

    /**
     * 将 Duration 转换为 HH:mm:ss 格式的字符串
     */
    public String formatDuration(Duration duration) {
        if (duration == null) {
            return "00:00:00";
        }

        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}