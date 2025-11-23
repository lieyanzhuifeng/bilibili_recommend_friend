// CoCommentRecommendationController.java
package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.dto.*;
import com.bilibili.rec_system.service.CommentBasedFriendRecommendationService;
import com.bilibili.rec_system.service.RecommendationService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*") // 允许所有跨域请求，方便测试
public class RecommendationController {

    @Autowired
    private RecommendationServiceFactory recommendationServiceFactory;

    @Autowired
    private CommentBasedFriendRecommendationService commentBasedFriendRecommendationService;

    /**
     * 获取同视频评论推荐用户
     */
    @GetMapping("/co-comment/{userId}")
    public List<CoCommentRecommendationDTO> getCoCommentRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("co_comment");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 转换为具体DTO类型
        return baseResult.stream()
                .map(dto -> (CoCommentRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 获取回复推荐用户
     */
    @GetMapping("/reply/{userId}")
    public List<BaseDTO> getReplyRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("reply");
        // 直接返回BaseDTO，不需要向下转型
        return service.recommendUsers(userId);
    }

    /**
     * 获取观看视频相似度推荐用户
     */
    @GetMapping("/shared-video/{userId}")
    public List<SharedVideoRecommendationDTO> getSharedVideoRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("shared_video_recommendation");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 转换为具体DTO类型
        return baseResult.stream()
                .map(dto -> (SharedVideoRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 获取分区重合度推荐用户
     */
    @GetMapping("/category/{userId}")
    public List<CategoryRecommendationDTO> getCategoryRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("category");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        return baseResult.stream()
                .map(dto -> (CategoryRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 获取内容类型重合度推荐用户
     */
    @GetMapping("/theme/{userId}")
    public List<ThemeRecommendationDTO> getThemeRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("theme");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        return baseResult.stream()
                .map(dto -> (ThemeRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 用户行为相似度推荐
     */
    @GetMapping("/user-behavior/{userId}")
    public List<UserBehaviorRecommendationDTO> getUserBehaviorRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("user_behavior");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        return baseResult.stream()
                .map(dto -> (UserBehaviorRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 共同关注UP主推荐
     */
    @GetMapping("/common-up/{userId}")
    public List<CommonUpRecommendationDTO> getCommonUpRecommendations(@PathVariable Long userId) {

        RecommendationService service = recommendationServiceFactory.getRecommendationService("common_up");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        return baseResult.stream()
                .map(dto -> (CommonUpRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 收藏夹相似度推荐
     */
    @GetMapping("/favorite-similarity/{userId}")
    public List<FavoriteSimilarityDTO> getFavoriteSimilarityRecommendations(@PathVariable Long userId) {

        RecommendationService service = recommendationServiceFactory.getRecommendationService("favorite_similarity");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        return baseResult.stream()
                .map(dto -> (FavoriteSimilarityDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 通过评论推荐好友
     */
    @GetMapping("/comment-friends/{userId}")
    public List<FriendRecommendationDTO> recommendFriendsByComments(@PathVariable Long userId) {
        return commentBasedFriendRecommendationService.recommendFriendsByComments(userId);
    }

}
