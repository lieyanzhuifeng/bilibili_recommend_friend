package com.bilibili.rec_system.service.FilterServiceImpl.FactoryMethod;

import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.FilterServiceImpl.UserActivityRecommendationService;
import org.springframework.stereotype.Component;
//具体工厂
@Component
public class UserActivityFilterFactory implements FilterFactoryMethod {

    @Override
    public FilterService createFilterService() {
        return new UserActivityRecommendationService();
    }
}