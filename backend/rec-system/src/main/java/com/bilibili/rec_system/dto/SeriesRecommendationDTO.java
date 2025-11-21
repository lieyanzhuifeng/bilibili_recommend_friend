package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeriesRecommendationDTO extends BaseDTO {
    private String seriesName;    // 系列名称

    public SeriesRecommendationDTO() {}

    public SeriesRecommendationDTO(Long userId, String username, String avatarPath,
                                   String seriesName) {
        super(userId, username, avatarPath);
        this.seriesName = seriesName;
    }
}