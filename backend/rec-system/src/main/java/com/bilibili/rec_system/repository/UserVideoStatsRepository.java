package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserVideoStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoStatsRepository extends JpaRepository<UserVideoStats, Long> {

    // 工具函数
    @Query("SELECT uvs.videoId FROM UserVideoStats uvs WHERE uvs.userId = :userId")
    List<Long> findWatchedVideoIdsByUser(@Param("userId") Long userId);

    @Query("SELECT uvs FROM UserVideoStats uvs WHERE uvs.userId = :userId AND uvs.videoId = :videoId")
    UserVideoStats findByUserAndVideo(@Param("userId") Long userId, @Param("videoId") Long videoId);

    // 3.6 深度观看推荐
    @Query("SELECT uvs FROM UserVideoStats uvs WHERE uvs.videoId = :videoId AND uvs.userId <> :userId")
    List<UserVideoStats> findByVideoExcludingUser(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 3.12 观看视频相似度
    @Query("SELECT uvs.userId, COUNT(uvs.videoId) FROM UserVideoStats uvs WHERE uvs.videoId IN (SELECT uvs2.videoId FROM UserVideoStats uvs2 WHERE uvs2.userId = :userId) AND uvs.userId <> :userId GROUP BY uvs.userId")
    List<Object[]> findSharedVideoCounts(@Param("userId") Long userId);
}