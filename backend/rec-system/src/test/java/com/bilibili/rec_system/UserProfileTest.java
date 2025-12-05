package com.bilibili.rec_system;

import com.bilibili.rec_system.service.RecommendationServiceImpl.*;
import com.bilibili.rec_system.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class UserProfileTest {
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private CategoryRecommendationService categoryRecommendationService;
    @Autowired
    private FavoriteSimilarityService favoriteSimilarityService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUserProfileServiceShow() {
        System.out.println("=== UserProfileService.show() 原始JSON输出 ===");

        Long userId = 1L;

        try {
            // 调用外观服务的show方法
            Map<String, Object> fullProfile = userProfileService.show(userId);

            // 使用JSON格式完整打印
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(fullProfile);

            System.out.println(jsonResult);

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}