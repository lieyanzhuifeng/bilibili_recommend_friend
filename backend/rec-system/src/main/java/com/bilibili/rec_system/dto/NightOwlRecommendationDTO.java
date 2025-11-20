package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NightOwlRecommendationDTO extends BaseDTO {
    private Integer watchHours;      // 观看小时数
    private Integer activeDays;      // 活跃天数

    public NightOwlRecommendationDTO() {}

    public NightOwlRecommendationDTO(Long userId, String username, String avatarPath,
                                     Integer watchHours, Integer activeDays) {
        super(userId, username, avatarPath);
        this.watchHours = watchHours;
        this.activeDays = activeDays;
    }
}