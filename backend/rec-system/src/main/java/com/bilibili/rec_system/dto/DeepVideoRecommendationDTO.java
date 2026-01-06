package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeepVideoRecommendationDTO extends BaseDTO {
    private Long videoId;
    private String videoTitle;
    private Double totalWatchDuration;  // 总观看时长（秒）
    private Integer watchCount;          // 观看次数

    public DeepVideoRecommendationDTO() {}

    public DeepVideoRecommendationDTO(Long userId, String username, String avatarPath,
                                      Long videoId, String videoTitle,
                                      Double totalWatchDuration, Integer watchCount) {
        super(userId, username, avatarPath);
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.totalWatchDuration = totalWatchDuration;
        this.watchCount = watchCount;
    }
}