package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Setter
@Getter
public class SameUpRecommendationDTO extends BaseDTO {
    // getters and setters
    private String upName;              // UP主名称
    private Duration totalWatchDuration; // 观看总时长(分钟)

    public SameUpRecommendationDTO() {}

    public SameUpRecommendationDTO(Long userId, String username, String avatarPath,
                                   String upName, Duration totalWatchDuration) {
        super(userId, username, avatarPath);
        this.upName = upName;
        this.totalWatchDuration = totalWatchDuration;
    }

}
