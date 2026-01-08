package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.service.CommentBasedFriendRecommendationServiceImpl;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//adapter实现，具体流程是把输出的数据进行调整，使得符合接口
@Service
public class CommentBasedFriendRecommendationServiceAdapter implements RecommendationService {

    @Autowired
    private CommentBasedFriendRecommendationServiceImpl commentBasedFriendRecommendationService;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        // 直接返回 FriendRecommendationDTO 列表
        // 由于 FriendRecommendationDTO 应该是 BaseDTO 的子类，这是安全的
        List<FriendRecommendationDTO> friendResults =
                commentBasedFriendRecommendationService.recommendFriendsByComments(userId);

        // 直接转换，因为 FriendRecommendationDTO 应该继承 BaseDTO
        return new ArrayList<BaseDTO>(friendResults);
    }
}