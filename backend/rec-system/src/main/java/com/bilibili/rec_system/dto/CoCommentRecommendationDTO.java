package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoCommentRecommendationDTO extends BaseDTO {
    private String videoTitle;

    public CoCommentRecommendationDTO() {}

    public CoCommentRecommendationDTO(Long userId, String username, String avatarPath) {
        super(userId, username, avatarPath);
    }

    public CoCommentRecommendationDTO(Long userId, String username, String avatarPath, String videoTitle) {
        super(userId, username, avatarPath);
        this.videoTitle = videoTitle;
    }
}

