package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserVideoStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public class UserVideoStatsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 工具函数：获取用户观看过的视频ID列表
     */
    public List<Long> findWatchedVideoIdsByUser(Long userId) {
        String sql = "SELECT videoID FROM user_video_stats WHERE userID = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    /**
     * 根据视频ID、最小观看次数和最小观看时长筛选用户
     * 注意：totalWatchDuration在数据库中是TIME类型，需要转换为小时数进行查询
     */
    public List<UserVideoStats> findByVideoIdAndMinCountOrDuration(Long videoId, Integer minCount, LocalTime minDuration) {
        // 将LocalTime转换为小时数
        double minHours = minDuration != null ? minDuration.toSecondOfDay() / 3600.0 : 0;

        // 关键：使用TIME_TO_SEC函数将数据库中的TIME类型转换为小时数进行查询
        String sql = "SELECT " +
                "statID as statId, " +
                "userID as userId, " +
                "videoID as videoId, " +
                "watchCount as watchCount, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration " +  // 转换为小时数
                "FROM user_video_stats WHERE videoID = ? " +
                "AND (watchCount >= ? OR TIME_TO_SEC(totalWatchDuration) / 3600.0 >= ?)";  // 在WHERE条件中也使用小时数比较
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserVideoStats.class),
                videoId, minCount, minHours);
    }

    /**
     * 根据用户ID和视频ID列表批量查询观看记录
     */
    public List<UserVideoStats> findByUserIdAndVideoIdIn(Long userId, List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", videoIds.stream().map(v -> "?").toArray(String[]::new));
        String sql = "SELECT " +
                "statID as statId, " +
                "userID as userId, " +
                "videoID as videoId, " +
                "watchCount as watchCount, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration " +  // 转换为小时数
                "FROM user_video_stats WHERE userID = ? AND videoID IN (" + placeholders + ")";

        Object[] params = new Object[videoIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < videoIds.size(); i++) {
            params[i + 1] = videoIds.get(i);
        }

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserVideoStats.class), params);
    }

    /**
     * 根据视频ID列表查询所有相关的观看记录
     */
    public List<UserVideoStats> findByVideoIdIn(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", videoIds.stream().map(v -> "?").toArray(String[]::new));
        String sql = "SELECT " +
                "statID as statId, " +
                "userID as userId, " +
                "videoID as videoId, " +
                "watchCount as watchCount, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration " +  // 转换为小时数
                "FROM user_video_stats WHERE videoID IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserVideoStats.class), videoIds.toArray());
    }

    /**
     * 根据用户ID查找观看记录
     */
    public List<UserVideoStats> findByUserId(Long userId) {
        String sql = "SELECT " +
                "statID as statId, " +
                "userID as userId, " +
                "videoID as videoId, " +
                "watchCount as watchCount, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration " +  // 转换为小时数
                "FROM user_video_stats WHERE userID = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserVideoStats.class), userId);
    }

    /**
     * 新增方法：直接使用小时数作为参数进行查询，避免LocalTime转换
     */
    public List<UserVideoStats> findByVideoIdAndMinCountOrDurationHours(Long videoId, Integer minCount, Double minDurationHours) {
        String sql = "SELECT " +
                "statID as statId, " +
                "userID as userId, " +
                "videoID as videoId, " +
                "watchCount as watchCount, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration " +  // 转换为小时数
                "FROM user_video_stats WHERE videoID = ? " +
                "AND (watchCount >= ? OR TIME_TO_SEC(totalWatchDuration) / 3600.0 >= ?)";  // 使用小时数比较
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserVideoStats.class),
                videoId, minCount, minDurationHours);
    }
}