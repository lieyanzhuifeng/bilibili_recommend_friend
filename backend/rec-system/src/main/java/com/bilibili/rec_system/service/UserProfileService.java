package com.bilibili.rec_system.service;

import com.bilibili.rec_system.service.FilterServiceImpl.DeepVideoFilterService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserProfileService {

    @Autowired
    private CategoryRecommendationService categoryRecommendationService;

    @Autowired
    private FavoriteSimilarityService favoriteSimilarityService;

    @Autowired
    private ThemeRecommendationService themeRecommendationService;

    @Autowired
    private UserBehaviorRecommendationService userBehaviorRecommendationService;

    @Autowired
    private CommonUpRecommendationService commonUpRecommendationService;

    // 新增：注入 DeepVideoFilterService
    @Autowired
    private DeepVideoFilterService deepVideoFilterService;

    public Map<String, Object> show(Long userId) {
        Map<String, Object> profile = new LinkedHashMap<>();

        // 1. 调用CategoryRecommendationService获取分区分布
        Map<String, Double> categoryDistribution = categoryRecommendationService.show(userId);
        profile.put("categoryDistribution", categoryDistribution);

        // 2. 调用ThemeRecommendationService获取主题分布
        Map<String, Double> themeDistribution = themeRecommendationService.show(userId);
        profile.put("themeDistribution", themeDistribution);

        // 3. 调用FavoriteSimilarityService获取收藏视频
        Map<Long, String> userFavorites = favoriteSimilarityService.show(userId);
        profile.put("favoriteVideos", userFavorites);

        // 4. 调用UserBehaviorRecommendationService获取行为统计
        Map<String, Object> behaviorStats = userBehaviorRecommendationService.show(userId);
        profile.put("behaviorStatistics", behaviorStats);

        // 5. 关注up主
        List<Map<String, Object>> followedUps = commonUpRecommendationService.show(userId);
        profile.put("followedUps", commonUpRecommendationService.show(userId));

        // 6. 新增：调用 DeepVideoFilterService 获取深度观看视频
        profile.put("deepWatchedVideos", deepVideoFilterService.show(userId));

        return profile;
    }
}