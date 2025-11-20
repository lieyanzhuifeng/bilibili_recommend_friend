package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameTagVideoCountDTO extends BaseDTO {
    // getters and setters
    private String tagName;              // 标签名称
    private Integer watchedVideoCount;   // 观看的视频数量
    private Integer totalVideoCount;     // 该标签的总视频数量
    private Double watchRatio;           // 观看比例

    public SameTagVideoCountDTO() {}

    public SameTagVideoCountDTO(Long userId, String username, String avatarPath,
                                String tagName, Integer watchedVideoCount,
                                Integer totalVideoCount, Double watchRatio) {
        super(userId, username, avatarPath);
        this.tagName = tagName;
        this.watchedVideoCount = watchedVideoCount;
        this.totalVideoCount = totalVideoCount;
        this.watchRatio = watchRatio;
    }

}