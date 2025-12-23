package com.bilibili.rec_system.service;

import com.bilibili.rec_system.dto.FriendRecommendationDTO;

import java.util.List;
//没有使用adapter的接口
public interface CommentBasedFriendRecommendationService {

    /**
     * 通过评论推荐好友
     * @param userId 目标用户ID
     * @return 推荐结果列表
     */
    List<FriendRecommendationDTO> recommendFriendsByComments(Long userId);

    /**
     * 获取最小评论字数配置
     * @return 最小评论字数
     */
    int getMinCommentLength();

    /**
     * 获取匹配分数阈值配置
     * @return 匹配分数阈值
     */
    double getMatchScoreThreshold();

    /**
     * 清理资源
     */
    void cleanup();
}