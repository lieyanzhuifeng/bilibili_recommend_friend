package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserTagStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTagStatsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据标签ID查找 - 返回包含小时数的实体
     */
    public List<UserTagStats> findByTagId(Long tagId) {
        String sql = "SELECT " +
                "tagStatID as tagStatId, " +
                "userID as userId, " +
                "tagID as tagId, " +
                "totalWatchDuration / 3600.0 as watchHours, " +  // 秒转小时
                "uniqueVideos as uniqueVideos " +
                "FROM user_tag_stats WHERE tagID = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserTagStats.class), tagId);
    }

    /**
     * 根据标签ID和小时数范围查找
     */
    public List<UserTagStats> findByTagIdAndHoursRange(Long tagId, Double minHours, Double maxHours) {
        String sql = "SELECT " +
                "tagStatID as tagStatId, " +
                "userID as userId, " +
                "tagID as tagId, " +
                "totalWatchDuration / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_tag_stats WHERE tagID = ? AND " +
                "totalWatchDuration / 3600.0 BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserTagStats.class), tagId, minHours, maxHours);
    }

    /**
     * 根据标签ID和最小时长查找
     */
    public List<UserTagStats> findByTagIdAndMinHours(Long tagId, Double minHours) {
        String sql = "SELECT " +
                "tagStatID as tagStatId, " +
                "userID as userId, " +
                "tagID as tagId, " +
                "totalWatchDuration / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_tag_stats WHERE tagID = ? AND " +
                "totalWatchDuration / 3600.0 >= ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserTagStats.class), tagId, minHours);
    }
}