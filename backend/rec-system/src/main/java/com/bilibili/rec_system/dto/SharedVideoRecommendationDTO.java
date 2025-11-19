package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SharedVideoRecommendationDTO extends BaseDTO {
    private Double similarityRate;  // 观看视频相似度

    public SharedVideoRecommendationDTO() {}

    public SharedVideoRecommendationDTO(Long userId, String username, String avatarPath, Double similarityRate) {
        super(userId, username, avatarPath);
        this.similarityRate = similarityRate;
    }

}