package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FilterServiceFactory {

    @Autowired
    private SameUpRecommendationService sameUpRecommendationService;
    // 可以继续添加其他筛选服务
    // @Autowired
    // private CategoryFilterService categoryFilterService;
    // @Autowired
    // private DurationFilterService durationFilterService;

    /**
     * 根据筛选类型获取对应的筛选服务
     * @param filterType 筛选类型
     * @return 筛选服务实例
     */
    public FilterService getFilterService(String filterType) {
        return switch (filterType) {
            case "same_up" -> sameUpRecommendationService;
            // case "category" -> categoryFilterService;
            // case "duration" -> durationFilterService;
            default -> throw new IllegalArgumentException("不支持的筛选类型: " + filterType);
        };
    }

    /**
     * 获取所有可用的筛选类型
     */
    public Map<String, String> getAvailableFilterTypes() {
        return Map.of(
                "same_up", "同一UP主筛选"
                // "category", "分区筛选",
                // "duration", "时长筛选"
        );
    }
}