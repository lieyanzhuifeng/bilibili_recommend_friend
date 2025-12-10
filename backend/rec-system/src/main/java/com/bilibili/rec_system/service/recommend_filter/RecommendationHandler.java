// RecommendationHandler.java
package com.bilibili.rec_system.service.recommend_filter;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.service.FilterServiceImpl.FilterServiceFactory;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecommendationHandler extends Handler {

    @Autowired
    private RecommendationServiceFactory recommendationFactory;

    @Autowired
    private FilterServiceFactory filterFactory;


    @Override
    protected void handleRequest(Request request) {
        System.out.println("[RecommendationHandler] 处理请求，类型: " + request.getRecommendationType());

        List<BaseDTO> results;
        String type = request.getRecommendationType();

        // 判断是推荐类型还是筛选类型
        if (isFilterType(type)) {
            results = handleFilterRequest(request, type);
        } else {
            results = handleRecommendationRequest(request, type);
        }

        request.setRecommendations(results);
        System.out.println("[RecommendationHandler] 操作完成，数量: " + results.size());
    }

    /**
     * 处理筛选类型的请求
     */
    private List<BaseDTO> handleFilterRequest(Request request, String type) {
        if (request.getFilterBaseDTO() == null) {
            System.out.println("[RecommendationHandler] 警告：筛选功能缺少FilterBaseDTO");
            // 尝试创建默认的FilterBaseDTO
            request.setFilterBaseDTO(new com.bilibili.rec_system.dto.filter.FilterBaseDTO());
        }

        try {
            // ✅ 通过工厂获取具体筛选服务
            var service = filterFactory.getFilterService(type);
            return service.filterUsers(request.getFilterBaseDTO());

        } catch (IllegalArgumentException e) {
            System.out.println("[RecommendationHandler] 错误：找不到筛选服务类型: " + type);
            return List.of();
        }
    }

    /**
     * 处理推荐类型的请求
     */
    private List<BaseDTO> handleRecommendationRequest(Request request, String type) {
        if (request.getUserId() == null) {
            System.out.println("[RecommendationHandler] 错误：推荐功能需要UserId");
            return List.of();
        }

        try {
            // ✅ 通过工厂获取具体推荐服务
            var service = recommendationFactory.getRecommendationService(type);
            return service.recommendUsers(request.getUserId());

        } catch (IllegalArgumentException e) {
            System.out.println("[RecommendationHandler] 错误：找不到推荐服务类型: " + type);
            return List.of();
        }
    }

    /**
     * 判断是否为筛选类型
     */
    private boolean isFilterType(String type) {
        try {
            // 尝试从筛选工厂获取，如果能获取到就是筛选类型
            filterFactory.getFilterService(type);
            return true;
        } catch (IllegalArgumentException e) {
            // 如果不是筛选类型，检查是否为推荐类型
            try {
                recommendationFactory.getRecommendationService(type);
                return false; // 是推荐类型
            } catch (IllegalArgumentException e2) {
                // 既不是筛选也不是推荐
                System.err.println("不支持的请求类型: " + type);
                return false;
            }
        }
    }
}