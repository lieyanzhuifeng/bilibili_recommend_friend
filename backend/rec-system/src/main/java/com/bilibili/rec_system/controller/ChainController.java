package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.dto.*;
import com.bilibili.rec_system.dto.filter.*;
import com.bilibili.rec_system.service.recommend_filter.FilterRecommendChainService;
import com.bilibili.rec_system.service.recommend_filter.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chain")
public class ChainController {

    @Autowired
    private FilterRecommendChainService chainService;

    // ========== 完全复用FilterController的API名字 ==========

    /**
     * 同一UP主筛选推荐 + 责任链筛选
     */
    @GetMapping("/same-up")
    public List<SameUpRecommendationDTO> filterSameUpUsersByGet(
            @RequestParam Long upId,
            @RequestParam(required = false, defaultValue = "-1") Integer durationOption,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        SameUpFilterDTO filter = new SameUpFilterDTO(upId, durationOption);
        Request request = new Request("same_up", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SameUpRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 同一标签筛选 + 责任链筛选
     */
    @GetMapping("/same-tag")
    public List<SameTagRecommendationDTO> filterSameTagUsers(
            @RequestParam Long tagId,
            @RequestParam(required = false, defaultValue = "-1") Integer durationOption,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        SameTagFilterDTO filter = new SameTagFilterDTO(tagId, durationOption);
        Request request = new Request("same_tag", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SameTagRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * UP主视频观看比例筛选 + 责任链筛选
     */
    @PostMapping("/same-up-video-count")
    public List<SameUpVideoCountDTO> getSameUpVideoCountRecommendations(
            @RequestBody SameUpVideoCountFilterDTO filter,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("same_up_video_count", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SameUpVideoCountDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 标签视频观看比例筛选 + 责任链筛选
     */
    @GetMapping("/same-tag-video-count")
    public List<SameTagVideoCountDTO> getSameTagVideoCountRecommendations(
            @RequestParam Long tagId,
            @RequestParam Integer ratioOption,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        SameTagVideoCountFilterDTO filter = new SameTagVideoCountFilterDTO(tagId, ratioOption);
        Request request = new Request("same_tag_video_count", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SameTagVideoCountDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 关注时间缘分推荐 + 责任链筛选
     */
    @GetMapping("/follow-time")
    public List<FollowTimeRecommendationDTO> getFollowTimeRecommendations(
            @RequestParam Long userId,
            @RequestParam Long upId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        FollowTimeFilterDTO filter = new FollowTimeFilterDTO(userId, upId);
        Request request = new Request("follow_time", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (FollowTimeRecommendationDTO) dto)
                .collect(Collectors.toList());
    }



    /**
     * 深度视频筛选 + 责任链筛选
     */
    @GetMapping("/deep-video")
    public List<DeepVideoRecommendationDTO> deepVideoFilter(
            @RequestParam Long videoId,
            @RequestParam Integer option,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        DeepVideoFilterDTO filter = new DeepVideoFilterDTO();
        filter.setVideoId(videoId);
        filter.setOption(option);

        Request request = new Request("deep_video", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (DeepVideoRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 系列作品筛选 + 责任链筛选
     */
    @GetMapping("/series")
    public List<SeriesRecommendationDTO> seriesFilter(
            @RequestParam Long tagId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        SeriesFilterDTO filter = new SeriesFilterDTO();
        filter.setTagId(tagId);

        Request request = new Request("series", null, filter, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SeriesRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    // ========== 新增：推荐类型API + 责任链筛选 ==========
    /**
     * 同视频评论推荐 + 责任链筛选
     */
    @GetMapping("/co-comment/{userId}")
    public List<CoCommentRecommendationDTO> getCoCommentRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("co_comment", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (CoCommentRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 回复推荐 + 责任链筛选
     */
    @GetMapping("/reply/{userId}")
    public List<BaseDTO> getReplyRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("reply", userId, null, activity, nightOwl);
        return chainService.executeFullChain(request);
    }

    /**
     * 视频相似度推荐 + 责任链筛选
     */
    @GetMapping("/shared-video/{userId}")
    public List<SharedVideoRecommendationDTO> getSharedVideoRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("shared_video_recommendation", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (SharedVideoRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 分区重合度推荐 + 责任链筛选
     */
    @GetMapping("/category/{userId}")
    public List<CategoryRecommendationDTO> getCategoryRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("category", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (CategoryRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 内容类型重合度推荐 + 责任链筛选
     */
    @GetMapping("/theme/{userId}")
    public List<ThemeRecommendationDTO> getThemeRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("theme", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (ThemeRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 用户行为相似度推荐 + 责任链筛选
     */
    @GetMapping("/user-behavior/{userId}")
    public List<UserBehaviorRecommendationDTO> getUserBehaviorRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("user_behavior", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (UserBehaviorRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 共同关注UP主推荐 + 责任链筛选
     */
    @GetMapping("/common-up/{userId}")
    public List<CommonUpRecommendationDTO> getCommonUpRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("common_up", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (CommonUpRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 收藏夹相似度推荐 + 责任链筛选
     */
    @GetMapping("/favorite-similarity/{userId}")
    public List<FavoriteSimilarityDTO> getFavoriteSimilarityRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        Request request = new Request("favorite_similarity", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (FavoriteSimilarityDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 通过评论推荐好友 + 责任链筛选
     */
    @GetMapping("/comment-friends/{userId}")
    public List<FriendRecommendationDTO> recommendFriendsByComments(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer activity,
            @RequestParam(required = false, defaultValue = "0") Integer nightOwl) {

        // 注意：这个服务可能不是通过工厂获取的，需要特殊处理
        // 这里假设它实现了RecommendationService接口
        Request request = new Request("comment_friends", userId, null, activity, nightOwl);
        List<BaseDTO> results = chainService.executeFullChain(request);

        return results.stream()
                .map(dto -> (FriendRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

}