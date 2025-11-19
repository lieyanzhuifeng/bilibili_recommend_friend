package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.context.Theme;

@Service
public class RecommendationServiceFactory {

    @Autowired
    private ReplyRecommendationService replyRecommendationService;
    @Autowired
    private CoCommentRecommendationService coCommentRecommendationService;
    @Autowired
    SharedVideoRecommendationService sharedVideoRecommendationService;
    @Autowired
    CategoryRecommendationService categoryRecommendationService;
    @Autowired
    ThemeRecommendationService themeRecommendationService;

    public RecommendationService getRecommendationService(String type) {
        return (RecommendationService) switch (type) {
            case "reply" -> replyRecommendationService;
            case "co_comment" -> coCommentRecommendationService;
            case "shared_video_recommendation" ->sharedVideoRecommendationService;
            case "category" -> categoryRecommendationService;
            case "theme" -> themeRecommendationService;
            default -> throw new IllegalArgumentException("不支持的推荐类型: " + type);
        };
    }

}