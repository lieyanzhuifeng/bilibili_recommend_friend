package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameTagRecommendationDTO extends BaseDTO {
    // getters and setters
    private String tagName;              // 标签名称
    private Double totalWatchHours;      // 观看总时长(小时)

    public SameTagRecommendationDTO() {}

    public SameTagRecommendationDTO(Long userId, String username, String avatarPath,
                                    String tagName, Double totalWatchHours) {
        super(userId, username, avatarPath);
        this.tagName = tagName;
        this.totalWatchHours = totalWatchHours;
    }

}