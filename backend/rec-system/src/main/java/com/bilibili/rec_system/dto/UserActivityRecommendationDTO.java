package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivityRecommendationDTO extends BaseDTO {
    private Double totalWatchHours;  // 总观看小时数
    private Integer activeDays;      // 活跃天数

    public UserActivityRecommendationDTO() {}

    public UserActivityRecommendationDTO(Long userId, String username, String avatarPath,
                                         Double totalWatchHours, Integer activeDays) {
        super(userId, username, avatarPath);
        this.totalWatchHours = totalWatchHours;
        this.activeDays = activeDays;
    }
}