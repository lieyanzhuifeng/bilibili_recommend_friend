package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameUpVideoCountDTO extends BaseDTO {
    // getters and setters
    private String upName;              // UP主名称
    private Integer watchedVideoCount;  // 观看的视频数量
    private Integer totalVideoCount;    // 该UP主的总视频数量
    private Double watchRatio;          // 观看比例

    public SameUpVideoCountDTO() {}

    public SameUpVideoCountDTO(Long userId, String username, String avatarPath,
                               String upName, Integer watchedVideoCount,
                               Integer totalVideoCount, Double watchRatio) {
        super(userId, username, avatarPath);
        this.upName = upName;
        this.watchedVideoCount = watchedVideoCount;
        this.totalVideoCount = totalVideoCount;
        this.watchRatio = watchRatio;
    }

}