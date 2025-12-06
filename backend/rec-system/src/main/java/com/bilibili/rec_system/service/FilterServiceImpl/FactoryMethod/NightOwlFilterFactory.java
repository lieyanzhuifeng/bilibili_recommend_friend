package com.bilibili.rec_system.service.FilterServiceImpl.FactoryMethod;

// NightOwl筛选的工厂

import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.FilterServiceImpl.NightOwlRecommendationService;
import org.springframework.stereotype.Component;

@Component
public class NightOwlFilterFactory implements FilterFactoryMethod {

    @Override
    public FilterService createFilterService() {
        return new NightOwlRecommendationService();
    }
}