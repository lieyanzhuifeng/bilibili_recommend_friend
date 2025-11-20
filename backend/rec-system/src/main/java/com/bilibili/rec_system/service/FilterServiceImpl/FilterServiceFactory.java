package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FilterServiceFactory {

    @Autowired
    private SameUpRecommendationService sameUpRecommendationService;

    @Autowired
    private SameTagRecommendationService sameTagRecommendationService;

    @Autowired
    private SameUpVideoCountService sameUpVideoCountService;

    @Autowired
    private SameTagVideoCountService sameTagVideoCountService;

    /**
     * 根据筛选类型获取对应的筛选服务
     * @param filterType 筛选类型
     * @return 筛选服务实例
     */
    public FilterService getFilterService(String filterType) {
        return switch (filterType) {
            case "same_up" -> sameUpRecommendationService;
            case "same_tag" -> sameTagRecommendationService;
            case "same_up_video_count" -> sameUpVideoCountService;
            case "same_tag_video_count" -> sameTagVideoCountService;
            default -> throw new IllegalArgumentException("不支持的筛选类型: " + filterType);
        };
    }

}