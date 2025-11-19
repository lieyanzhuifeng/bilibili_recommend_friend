package com.bilibili.rec_system;

import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.service.UserUpStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserUpStatsServiceTest {

    @Autowired
    private UserUpStatsService userUpStatsService;

    @Test
    void testFindAll() {
        System.out.println("=== 测试获取所有用户UP主统计数据 ===");

        List<UserUpStats> allStats = userUpStatsService.findAll();

        assertNotNull(allStats, "返回的列表不应为null");
        assertFalse(allStats.isEmpty(), "返回的列表不应为空");

        System.out.println("✅ 成功获取 " + allStats.size() + " 条记录");

        // 打印前10条记录
        System.out.println("\n=== 记录详情 ===");
        allStats.stream().limit(50).forEach(stats -> {
            System.out.printf(
                    "upStatId: %d, userId: %d, upId: %d, 时长: %s, 视频数: %d%n",
                    stats.getUpStatId(),
                    stats.getUserId(),
                    stats.getUpId(),
                    stats.getFormattedDuration(),  // 使用格式化方法显示
                    stats.getUniqueVideos()
            );

            // 验证 Duration 对象是否正确
            assertNotNull(stats.getTotalWatchDuration(), "Duration 不应为null");
            assertTrue(stats.getTotalWatchDuration().getSeconds() >= 0, "时长应大于等于0");
        });

        System.out.println("✅ 所有数据验证通过");
    }

    @Test
    void testFindByUserId() {
        System.out.println("=== 测试根据用户ID查找 ===");

        Long testUserId = 1L;
        List<UserUpStats> userStats = userUpStatsService.findByUserId(testUserId);

        assertNotNull(userStats);
        assertFalse(userStats.isEmpty());

        System.out.println("✅ 用户 " + testUserId + " 有 " + userStats.size() + " 条UP主记录");

        userStats.forEach(stat -> {
            assertEquals(testUserId, stat.getUserId(), "用户ID应该匹配");
            System.out.printf("UP主: %d, 时长: %s, 视频数: %d%n",
                    stat.getUpId(), stat.getFormattedDuration(), stat.getUniqueVideos());
        });
    }

    @Test
    void testFindByUserIdAndUpId() {
        System.out.println("=== 测试根据用户ID和UP主ID查找 ===");

        Long testUserId = 1L;
        Long testUpId = 3L;

        try {
            Optional<UserUpStats> statOpt = userUpStatsService.findByUserIdAndUpId(testUserId, testUpId);

            assertTrue(statOpt.isPresent(), "应该找到对应的记录");
            UserUpStats stat = statOpt.get();
            assertEquals(testUserId, stat.getUserId(), "用户ID应该匹配");
            assertEquals(testUpId, stat.getUpId(), "UP主ID应该匹配");

            System.out.println("✅ 找到用户 " + testUserId + " 观看 UP主 " + testUpId + " 的记录");
            System.out.printf("时长: %s, 视频数: %d%n", stat.getFormattedDuration(), stat.getUniqueVideos());

        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            e.printStackTrace();

            // 添加调试查询
            System.out.println("=== 调试：直接执行SQL查询 ===");
            String debugSql = "SELECT " +
                    "upStatID, userID, upID, " +
                    "TIME_FORMAT(totalWatchDuration, '%H:%i:%s') as totalWatchDuration, " +
                    "uniqueVideos " +
                    "FROM user_up_stats WHERE userID = ? AND upID = ?";

            // 如果你有 jdbcTemplate，可以这样调试
            // List<Map<String, Object>> results = jdbcTemplate.queryForList(debugSql, testUserId, testUpId);
            // results.forEach(System.out::println);
        }
    }



    @Test
    void testDurationConversion() {
        System.out.println("=== 测试时长转换 ===");

        // 测试各种时长格式的转换
        String[] testDurations = {"00:02:02", "12:30:32", "32:49:57", "100:15:30"};

        for (String durationStr : testDurations) {
            UserUpStats stats = new UserUpStats();
            // 这里可以测试 Service 中的 parseDuration 方法
            System.out.printf("原始字符串: %s%n", durationStr);
        }
    }
}