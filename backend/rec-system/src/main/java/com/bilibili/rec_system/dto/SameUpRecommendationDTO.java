package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Setter
@Getter
public class SameUpRecommendationDTO extends BaseDTO {
    private String upName;
    private Double watchHours;  // 改为 Double 类型

    public SameUpRecommendationDTO(Long userId, String username, String avatarPath,
                                   String upName, Double watchHours) {
        super(userId, username, avatarPath);
        this.upName = upName;
        this.watchHours = watchHours;
    }
}
