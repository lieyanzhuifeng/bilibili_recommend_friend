package com.bilibili.rec_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class VideoStatsTest{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testRepositoryQuery() {
        System.out.println("=== 测试UserVideoStatsRepository.findByUserId ===");

        // 方法1：直接测试Repository方法
        String userId = "1";

        // 先测试原始SQL，确保数据存在
        String countSql = "SELECT COUNT(*) FROM user_video_stats WHERE userID = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        System.out.println("用户 " + userId + " 的总记录数: " + count);

        if (count > 0) {
            String sql = "SELECT " +
                    "statID as statId, " +
                    "userID as userId, " +
                    "videoID as videoId, " +
                    "watchCount as watchCount, " +
                    "TIME_TO_SEC(totalWatchDuration) / 3600.0 as totalWatchDuration, " +
                    "CAST(totalWatchDuration AS CHAR) as originalTime " +
                    "FROM user_video_stats WHERE userID = ? LIMIT 100";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);

            System.out.println("=== 前" + Math.min(results.size(), 100) + "条数据 ===");
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> row = results.get(i);
                System.out.println("第" + (i + 1) + "行:");
                System.out.println("  statId: " + row.get("statId"));
                System.out.println("  userId: " + row.get("userId"));
                System.out.println("  videoId: " + row.get("videoId"));
                System.out.println("  watchCount: " + row.get("watchCount"));
                System.out.println("  原始TIME: " + row.get("originalTime"));
                System.out.println("  小时数: " + row.get("totalWatchDuration"));
                System.out.println("---");
            }
        } else {
            System.out.println("用户 " + userId + " 没有观看记录");
        }
    }



}