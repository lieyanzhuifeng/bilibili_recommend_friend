package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserVideoStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface UserVideoStatsRepository extends JpaRepository<UserVideoStats, Long> {

    // 工具函数
    @Query("SELECT uvs.videoId FROM UserVideoStats uvs WHERE uvs.userId = :userId")
    List<Long> findWatchedVideoIdsByUser(@Param("userId") Long userId);

    /**
     * 根据视频ID、最小观看次数和最小观看时长筛选用户
     */
    @Query("SELECT uvs FROM UserVideoStats uvs WHERE uvs.videoId = :videoId AND (uvs.watchCount >= :minCount OR uvs.totalWatchDuration >= :minDuration)")
    List<UserVideoStats> findByVideoIdAndMinCountOrDuration(@Param("videoId") Long videoId,
                                                            @Param("minCount") Integer minCount,
                                                            @Param("minDuration") LocalTime minDuration);

    /**
     * 根据用户ID和视频ID列表批量查询观看记录
     */
    @Query("SELECT uvs FROM UserVideoStats uvs WHERE uvs.userId = :userId AND uvs.videoId IN :videoIds")
    List<UserVideoStats> findByUserIdAndVideoIdIn(@Param("userId") Long userId, @Param("videoIds") List<Long> videoIds);

    /**
     * 根据视频ID列表查询所有相关的观看记录
     */
    @Query("SELECT uvs FROM UserVideoStats uvs WHERE uvs.videoId IN :videoIds")
    List<UserVideoStats> findByVideoIdIn(@Param("videoIds") List<Long> videoIds);

    List<UserVideoStats> findByUserId(Long userId);
}