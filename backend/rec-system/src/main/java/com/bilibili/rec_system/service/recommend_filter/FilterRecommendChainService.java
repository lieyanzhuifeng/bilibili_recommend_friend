// FilterRecommendChainService.java
package com.bilibili.rec_system.service.recommend_filter;

import com.bilibili.rec_system.dto.BaseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterRecommendChainService {

    @Autowired
    private RecommendationHandler recommendationHandler;

    @Autowired
    private UserActivityHandler userActivityHandler;

    @Autowired
    private NightOwlHandler nightOwlHandler;

    /**
     * 执行完整责任链：推荐 → 活跃度筛选 → 夜猫子筛选
     */
    public List<BaseDTO> executeFullChain(Request request) {

        // 构建责任链
        recommendationHandler.setNextHandler(userActivityHandler);
        userActivityHandler.setNextHandler(nightOwlHandler);

        // 执行
        recommendationHandler.submit(request);

        return request.getRecommendations();
    }


}