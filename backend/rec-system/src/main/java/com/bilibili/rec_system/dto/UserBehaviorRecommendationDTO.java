package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBehaviorRecommendationDTO extends BaseDTO {
    private Double similarityScore;     // 行为相似度分数 (0-1)

    public UserBehaviorRecommendationDTO() {}

    public UserBehaviorRecommendationDTO(Long userId, String username, String avatarPath,
                                         Double similarityScore) {
        super(userId, username, avatarPath);
        this.similarityScore = similarityScore;
    }
}