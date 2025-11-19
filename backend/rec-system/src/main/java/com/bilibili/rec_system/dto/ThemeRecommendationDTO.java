package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ThemeRecommendationDTO extends BaseDTO {
    private Double themeMatchScore;        // 各主题相似度直接相加的总分
    private List<String> commonThemes;     // 共同主题名称列表

    public ThemeRecommendationDTO() {}

    public ThemeRecommendationDTO(Long userId, String username, String avatarPath,
                                  Double themeMatchScore, List<String> commonThemes) {
        super(userId, username, avatarPath);
        this.themeMatchScore = themeMatchScore;
        this.commonThemes = commonThemes;
    }
}