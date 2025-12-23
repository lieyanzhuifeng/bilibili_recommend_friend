package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.service.CommentBasedFriendRecommendationServiceImpl;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
//adapter实现，具体流程是把输出的数据进行调整，使得符合接口
@Service
public class CommentBasedFriendRecommendationServiceAdapter implements RecommendationService {

    @Autowired
    private CommentBasedFriendRecommendationServiceImpl commentBasedFriendRecommendationService;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        // 获取队友的服务结果
        List<FriendRecommendationDTO> friendResults =
                commentBasedFriendRecommendationService.recommendFriendsByComments(userId);

        // 手动转换为BaseDTO
        return friendResults.stream()
                .map(this::convertToBaseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将FriendRecommendationDTO转换为BaseDTO
     */
    private BaseDTO convertToBaseDTO(FriendRecommendationDTO friendDTO) {
        if (friendDTO == null || friendDTO.getRecommendedUser() == null) {
            return new BaseDTO();
        }

        // 从recommendedUser中提取信息
        return new BaseDTO(
                friendDTO.getRecommendedUser().getUserId(),
                friendDTO.getRecommendedUser().getUsername(),
                friendDTO.getRecommendedUser().getAvatarPath()
        );
    }
}