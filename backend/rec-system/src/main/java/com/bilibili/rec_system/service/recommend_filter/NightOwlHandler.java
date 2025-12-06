// NightOwlHandler.java
package com.bilibili.rec_system.service.recommend_filter;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.entity.UserStatistics;
import com.bilibili.rec_system.repository.UserStatisticsRepository;
import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.FilterServiceImpl.FactoryMethod.NightOwlFilterFactory;
import com.bilibili.rec_system.service.FilterServiceImpl.NightOwlRecommendationService;
import com.bilibili.rec_system.service.FilterServiceImpl.UserActivityRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NightOwlHandler extends Handler {

    @Autowired
    private UserStatisticsRepository userStatsRepository;

    @Autowired
    private NightOwlFilterFactory nightOwlFilterFactory;


    @Override
    protected void handleRequest(Request request) {
        if (request.getNightOwl() == 0) {
            return; // 不筛选
        }

        // 在方法内部获取服务，避免初始化时的空指针
        FilterService filterService = nightOwlFilterFactory.createFilterService();
        NightOwlRecommendationService nightOwlService =
                (NightOwlRecommendationService) filterService;

        List<BaseDTO> filtered = request.getRecommendations().stream()
                .filter(dto -> filterNightOwl(dto.getUserId(), request.getNightOwl(), nightOwlService))
                .collect(Collectors.toList());

        request.setRecommendations(filtered);
    }

    private boolean filterNightOwl(Long userId, Integer option,
                                   NightOwlRecommendationService nightOwlService) {
        // 1. 获取用户统计
        UserStatistics stats = userStatsRepository.findByUserId(userId);
        if (stats == null) return false;

        // 2. 计算用户等级（复用已有逻辑）
        String owlType = nightOwlService.calculateOwlLevel(stats);

        // 3. 根据option判断
        return matchesOption(owlType, option);
    }

    private boolean matchesOption(String owlType, Integer option) {
        switch (option) {
            case 1: return "non_owl".equals(owlType);
            case 2: return "light_owl".equals(owlType);
            case 3: return "medium_owl".equals(owlType);
            case 4: return "heavy_owl".equals(owlType);

            // 两两组合
            case 5: return "non_owl".equals(owlType) || "light_owl".equals(owlType);
            case 6: return "non_owl".equals(owlType) || "medium_owl".equals(owlType);
            case 7: return "non_owl".equals(owlType) || "heavy_owl".equals(owlType);
            case 8: return "light_owl".equals(owlType) || "medium_owl".equals(owlType);
            case 9: return "light_owl".equals(owlType) || "heavy_owl".equals(owlType);
            case 10: return "medium_owl".equals(owlType) || "heavy_owl".equals(owlType);

            // 三种组合
            case 11: return "non_owl".equals(owlType) || "light_owl".equals(owlType) || "medium_owl".equals(owlType);
            case 12: return "non_owl".equals(owlType) || "light_owl".equals(owlType) || "heavy_owl".equals(owlType);
            case 13: return "non_owl".equals(owlType) || "medium_owl".equals(owlType) || "heavy_owl".equals(owlType);
            case 14: return "light_owl".equals(owlType) || "medium_owl".equals(owlType) || "heavy_owl".equals(owlType);

            // 全部四种
            case 15: return true;

            default: return false;
        }
    }
}