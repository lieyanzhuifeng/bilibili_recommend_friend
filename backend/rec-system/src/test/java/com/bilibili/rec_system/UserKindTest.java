package com.bilibili.rec_system;

import com.bilibili.rec_system.entity.UserStatistics;
import com.bilibili.rec_system.repository.UserStatisticsRepository;
import com.bilibili.rec_system.service.FilterServiceImpl.NightOwlRecommendationService;
import com.bilibili.rec_system.service.FilterServiceImpl.UserActivityRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserKindTest {

    @Autowired
    private UserStatisticsRepository userStatsRepository;

    @Autowired
    private UserActivityRecommendationService userActivityService;

    @Autowired
    private NightOwlRecommendationService nightOwlService;

    @Test
    void testUserActivityAndNightOwlTypes() {
        System.out.println("=== 用户1-10的活动类型和夜猫子类型 ===");

        for (long userId = 1; userId <= 10; userId++) {
            UserStatistics stats = userStatsRepository.findByUserId(userId);

            if (stats == null) {
                System.out.println("用户" + userId + ": 无统计数据");
                continue;
            }

            String activityType = userActivityService.calculateUserLevel(stats);
            String nightOwlType = nightOwlService.calculateOwlLevel(stats);

            System.out.println("用户" + userId + ": " + activityType + " / " + nightOwlType);
        }
    }


}