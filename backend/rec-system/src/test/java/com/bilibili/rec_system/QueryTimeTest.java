package com.bilibili.rec_system;
import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.repository.UserUpStatsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class QueryTimeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserUpStatsRepository userUpStatsRepository;

    @Test
    void testTimeConversion() {
        String sql = "SELECT " +
                "CAST(totalWatchDuration AS CHAR) as originalTime, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as hours " +
                "FROM user_video_stats LIMIT 100";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        System.out.println("=== 前30条数据转换结果 ===");
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> row = results.get(i);
            System.out.println("第" + (i + 1) + "行:");
            System.out.println("  原始TIME: " + row.get("originalTime"));
            System.out.println("  转换后小时数: " + row.get("hours"));
            System.out.println("---");
        }
    }

    @Test
    void testJdbcTemplateToEntity() {
        String sql = "SELECT " +
                "upStatID as upStatId, " +
                "userID as userId, " +
                "upID as upId, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_up_stats WHERE upID = 11 LIMIT 10";

        List<UserUpStats> results = jdbcTemplate.query(sql, new RowMapper<UserUpStats>() {
            @Override
            public UserUpStats mapRow(ResultSet rs, int rowNum) throws SQLException {
                UserUpStats entity = new UserUpStats();
                entity.setUpStatId(rs.getLong("upStatId"));
                entity.setUserId(rs.getLong("userId"));
                entity.setUpId(rs.getLong("upId"));
                entity.setWatchHours(rs.getDouble("watchHours"));  // 手动设置 @Transient 字段
                entity.setUniqueVideos(rs.getInt("uniqueVideos"));
                return entity;
            }
        });

        System.out.println("=== jdbcTemplate 映射到 Entity 结果 ===");
        for (UserUpStats entity : results) {
            System.out.println("upStatId: " + entity.getUpStatId());
            System.out.println("userId: " + entity.getUserId());
            System.out.println("upId: " + entity.getUpId());
            System.out.println("watchHours: " + entity.getWatchHours());
            System.out.println("uniqueVideos: " + entity.getUniqueVideos());
            System.out.println("---");
        }
    }


    @Test
    void testBeanPropertyMapper() {
        String sql = "SELECT " +
                "upStatID as upStatId, " +
                "userID as userId, " +
                "upID as upId, " +
                "TIME_TO_SEC(totalWatchDuration) / 3600.0 as watchHours, " +
                "uniqueVideos as uniqueVideos " +
                "FROM user_up_stats WHERE upID = 11 LIMIT 10";

        List<UserUpStats> results = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(UserUpStats.class));

        System.out.println("=== BeanPropertyRowMapper 结果 ===");
        for (UserUpStats entity : results) {
            System.out.println("watchHours: " + entity.getWatchHours());
        }
    }







    @Test
    void testRepositoryToEntityMapping() {
        System.out.println("=== 测试 Repository 到 Entity 的映射 ===");

        Long testUpId = 11L;

        // 使用 Repository 查询
        List<UserUpStats> upStatsList = userUpStatsRepository.findByUpId(testUpId);

        System.out.println("UP主 " + testUpId + " 共有 " + upStatsList.size() + " 条记录");

        if (!upStatsList.isEmpty()) {
            System.out.println("\n=== 前5条记录的详细数据 ===");
            upStatsList.stream().limit(5).forEach(stats -> {
                System.out.println("记录详情:");
                System.out.println("  upStatId: " + stats.getUpStatId());
                System.out.println("  userId: " + stats.getUserId());
                System.out.println("  upId: " + stats.getUpId());
                System.out.println("  watchHours: " + stats.getWatchHours());
                System.out.println("  uniqueVideos: " + stats.getUniqueVideos());

                // 检查是否为 null
                if (stats.getWatchHours() == null) {
                    System.out.println("  ❌ watchHours 为 null!");
                } else {
                    System.out.println("  ✅ watchHours 有值: " + stats.getWatchHours());
                    // 转换回分钟验证
                    double minutes = stats.getWatchHours() * 60;
                    System.out.println("  相当于: " + minutes + " 分钟");
                }
                System.out.println("---");
            });
        } else {
            System.out.println("❌ 没有找到UP主 " + testUpId + " 的记录");
        }


    }


}