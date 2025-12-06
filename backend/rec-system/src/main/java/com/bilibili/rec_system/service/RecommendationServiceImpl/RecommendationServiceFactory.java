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
    @Autowired
    private UserBehaviorRecommendationService userBehaviorRecommendationService;
    @Autowired
    private CommonUpRecommendationService commonUpRecommendationService;
    @Autowired
    private FavoriteSimilarityService favoriteSimilarityService;
    @Autowired
    private CommentBasedFriendRecommendationServiceAdapter commentFriendAdapter;
    public RecommendationService getRecommendationService(String type) {
        return (RecommendationService) switch (type) {
            case "reply" -> replyRecommendationService;
            case "co_comment" -> coCommentRecommendationService;
            case "shared_video_recommendation" ->sharedVideoRecommendationService;
            case "category" -> categoryRecommendationService;
            case "theme" -> themeRecommendationService;
            case "user_behavior" -> userBehaviorRecommendationService;
            case "common_up" -> commonUpRecommendationService;
            case "favorite_similarity" -> favoriteSimilarityService;
            case "comment_friends" -> commentFriendAdapter;
            default -> throw new IllegalArgumentException("不支持的推荐类型: " + type);
        };
    }

}