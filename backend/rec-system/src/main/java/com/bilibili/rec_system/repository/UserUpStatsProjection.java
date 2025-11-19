package com.bilibili.rec_system.repository;

// 可以放在同一个文件中，或者单独创建
public interface UserUpStatsProjection {
    Long getUpStatId();
    Long getUserId();
    Long getUpId();
    String getTotalWatchStr();
    Integer getUniqueVideos();
}
