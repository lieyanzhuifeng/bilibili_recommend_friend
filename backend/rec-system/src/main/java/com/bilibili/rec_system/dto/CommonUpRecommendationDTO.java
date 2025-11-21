package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CommonUpRecommendationDTO extends BaseDTO {
    private Integer commonUpCount;      // 共同关注的UP主个数
    private List<String> commonUpNames; // 共同关注的UP主名称列表

    public CommonUpRecommendationDTO() {}

    public CommonUpRecommendationDTO(Long userId, String username, String avatarPath,
                                     Integer commonUpCount, List<String> commonUpNames) {
        super(userId, username, avatarPath);
        this.commonUpCount = commonUpCount;
        this.commonUpNames = commonUpNames;
    }
}