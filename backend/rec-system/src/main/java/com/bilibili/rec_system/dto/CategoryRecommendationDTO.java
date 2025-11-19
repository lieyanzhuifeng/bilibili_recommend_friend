package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CategoryRecommendationDTO extends BaseDTO {
    // getters and setters
    private Double categoryMatchScore;     // 各分区相似度直接相加的总分
    private List<String> commonCategories; // 共同分区名称列表

    public CategoryRecommendationDTO() {}

    public CategoryRecommendationDTO(Long userId, String username, String avatarPath,
                                     Double categoryMatchScore, List<String> commonCategories) {
        super(userId, username, avatarPath);
        this.categoryMatchScore = categoryMatchScore;
        this.commonCategories = commonCategories;
    }

}
