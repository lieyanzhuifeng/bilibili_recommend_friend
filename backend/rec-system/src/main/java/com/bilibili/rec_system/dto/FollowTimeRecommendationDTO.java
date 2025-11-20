package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowTimeRecommendationDTO extends BaseDTO {
    private String recommendationReason;  // 推荐理由
    private Integer recommendationScore;  // 推荐分数 (0-10)

    public FollowTimeRecommendationDTO() {}

    public FollowTimeRecommendationDTO(Long userId, String username, String avatarPath,
                                       String recommendationReason, Integer recommendationScore) {
        super(userId, username, avatarPath);
        this.recommendationReason = recommendationReason;
        this.recommendationScore = recommendationScore;
    }
}