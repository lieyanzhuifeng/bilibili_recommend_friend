// UserActivityHandler.java
package com.bilibili.rec_system.service.recommend_filter;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.entity.UserStatistics;
import com.bilibili.rec_system.repository.UserStatisticsRepository;
import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.FilterServiceImpl.FactoryMethod.UserActivityFilterFactory;
import com.bilibili.rec_system.service.FilterServiceImpl.UserActivityRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//handler子类，作为工作流的具体一环
@Component
public class UserActivityHandler extends Handler {

    @Autowired
    private UserStatisticsRepository userStatsRepository;

    @Autowired
    private UserActivityFilterFactory userActivityFilterFactory;

    @Override
    protected void handleRequest(Request request) {
        if (request.getUserActivity() == 0) {
            return; // 不筛选
        }

        // 在方法内部获取服务，避免初始化时的空指针
        FilterService filterService = userActivityFilterFactory.createFilterService();
        UserActivityRecommendationService userActivityService =
                (UserActivityRecommendationService) filterService;

        List<BaseDTO> filtered = request.getRecommendations().stream()
                .filter(dto -> filterUser(dto.getUserId(), request.getUserActivity(), userActivityService))
                .collect(Collectors.toList());

        request.setRecommendations(filtered);
    }

    private boolean filterUser(Long userId, Integer option,
                               UserActivityRecommendationService userActivityService) {
        // 1. 获取用户统计
        UserStatistics stats = userStatsRepository.findByUserId(userId);
        if (stats == null) return false;

        // 2. 计算用户等级（复用已有逻辑）
        String userLevel = userActivityService.calculateUserLevel(stats);

        // 3. 根据option判断
        return matchesOption(userLevel, option);
    }

    private boolean matchesOption(String userLevel, Integer option) {
        switch (option) {
            case 1: return "light_user".equals(userLevel);
            case 2: return "medium_user".equals(userLevel);
            case 3: return "heavy_user".equals(userLevel);
            case 4: return "light_user".equals(userLevel) || "medium_user".equals(userLevel);
            case 5: return "medium_user".equals(userLevel) || "heavy_user".equals(userLevel);
            case 6: return "light_user".equals(userLevel) || "heavy_user".equals(userLevel);
            case 7: return true; // 全部三种
            default: return false;
        }
    }
}