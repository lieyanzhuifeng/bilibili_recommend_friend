package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommendationServiceFactory {

    @Autowired
    private ReplyRecommendationService replyRecommendationService;

    @Autowired
    private CoCommentRecommendationService coCommentRecommendationService;

    public RecommendationService getRecommendationService(String type) {
        return switch (type) {
            case "reply" -> replyRecommendationService;
            case "co_comment" -> coCommentRecommendationService;
            default -> throw new IllegalArgumentException("不支持的推荐类型: " + type);
        };
    }

}