package com.bilibili.rec_system.service.FilterServiceImpl.FactoryMethod;

import com.bilibili.rec_system.service.FilterService;
import org.springframework.stereotype.Component;
//工厂方法
@Component
public interface FilterFactoryMethod {
    FilterService createFilterService();
}
