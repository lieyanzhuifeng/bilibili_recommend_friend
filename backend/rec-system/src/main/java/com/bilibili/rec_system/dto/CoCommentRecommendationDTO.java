package com.bilibili.rec_system.dto;

import java.util.List;

public class CoCommentRecommendationDTO extends BaseDTO {
    private String videoTitle;  // 改为单个视频标题，而不是列表

    public CoCommentRecommendationDTO() {}

    public CoCommentRecommendationDTO(Long userId, String username, String avatarPath, String videoTitle) {
        super(userId, username, avatarPath);
        this.videoTitle = videoTitle;
    }

    // getters and setters
    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }
}