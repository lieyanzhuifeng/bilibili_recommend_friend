package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteSimilarityDTO extends BaseDTO {
    private Double similarityScore;  // 收藏夹相似度 (0-1)

    public FavoriteSimilarityDTO() {}

    public FavoriteSimilarityDTO(Long userId, String username, String avatarPath,
                                 Double similarityScore) {
        super(userId, username, avatarPath);
        this.similarityScore = similarityScore;
    }
}