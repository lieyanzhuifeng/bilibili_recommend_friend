package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserUpStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserUpStatsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据UP主ID查找 - 返回包含小时数的实体
     */
    public List<UserUpStats> findByUpId(Long upId) {
        String sql = "SELECT " +
                "upStatID as upStatId, " +
                "userID as userId, " +
                "upID as upId, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_up_stats WHERE upID = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserUpStats.class), upId);
    }

    /**
     * 根据UP主ID和小时数范围查找
     */
    public List<UserUpStats> findByUpIdAndHoursRange(Long upId, Double minHours, Double maxHours) {
        String sql = "SELECT " +
                "upStatID as upStatId, " +
                "userID as userId, " +
                "upID as upId, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_up_stats WHERE upID = ? AND " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserUpStats.class), upId, minHours, maxHours);
    }

    /**
     * 根据UP主ID和最小时长查找
     */
    public List<UserUpStats> findByUpIdAndMinHours(Long upId, Double minHours) {
        String sql = "SELECT " +
                "upStatID as upStatId, " +
                "userID as userId, " +
                "upID as upId, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_up_stats WHERE upID = ? AND " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 >= ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserUpStats.class), upId, minHours);
    }
}