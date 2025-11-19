package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserUpStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserUpStatsRepository extends JpaRepository<UserUpStats, Long> {

    /**
     * 获取所有数据 - 使用投影
     */
    @Query(value = "SELECT " +
            "upStatID as upStatId, " +
            "userID as userId, " +
            "upID as upId, " +
            "TIME_FORMAT(totalWatchDuration, '%H:%i:%s') as totalWatchStr, " +  // 修正字段名
            "uniqueVideos as uniqueVideos " +  // 修正字段名
            "FROM user_up_stats",
            nativeQuery = true)
    List<UserUpStatsProjection> findAllWithFormattedDuration();

    /**
     * 根据用户ID查找 - 使用投影
     */
    @Query(value = "SELECT " +
            "upStatID as upStatId, " +
            "userID as userId, " +
            "upID as upId, " +
            "TIME_FORMAT(totalWatchDuration, '%H:%i:%s') as totalWatchStr, " +  // 修正字段名
            "uniqueVideos as uniqueVideos " +  // 修正字段名
            "FROM user_up_stats WHERE userID = :userId",
            nativeQuery = true)
    List<UserUpStatsProjection> findByUserIdWithFormattedDuration(@Param("userId") Long userId);

    /**
     * 根据UP主ID查找 - 使用投影
     */
    @Query(value = "SELECT " +
            "upStatID as upStatId, " +
            "userID as userId, " +
            "upID as upId, " +
            "TIME_FORMAT(totalWatchDuration, '%H:%i:%s') as totalWatchStr, " +  // 修正字段名
            "uniqueVideos as uniqueVideos " +  // 修正字段名
            "FROM user_up_stats WHERE upID = :upId",
            nativeQuery = true)
    List<UserUpStatsProjection> findByUpIdWithFormattedDuration(@Param("upId") Long upId);

    /**
     * 根据用户ID和UP主ID查找 - 使用投影
     */
    @Query(value = "SELECT " +
            "upStatID as upStatId, " +
            "userID as userId, " +
            "upID as upId, " +
            "TIME_FORMAT(totalWatchDuration, '%H:%i:%s') as totalWatchStr, " +  // 修正字段名
            "uniqueVideos as uniqueVideos " +  // 修正字段名
            "FROM user_up_stats WHERE userID = :userId AND upID = :upId",
            nativeQuery = true)
    Optional<UserUpStatsProjection> findByUserIdAndUpIdWithFormattedDuration(@Param("userId") Long userId, @Param("upId") Long upId);
}