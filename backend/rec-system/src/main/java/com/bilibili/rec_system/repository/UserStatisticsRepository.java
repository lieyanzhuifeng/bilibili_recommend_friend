package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    UserStatistics findByUserId(Long userId);

    // 3.4 行为相似度推荐
    @Query("SELECT us FROM UserStatistics us WHERE us.userId <> :userId")
    List<UserStatistics> findAllExceptUser(@Param("userId") Long userId);

    // 3.5 夜猫子推荐
    @Query("SELECT us.userId FROM UserStatistics us WHERE us.userId <> :userId ORDER BY ABS(us.nightWatchMinutes - :targetMinutes) ASC, ABS(us.activeDays - :targetDays) ASC")
    List<Long> findSimilarNightOwlUsers(@Param("userId") Long userId, @Param("targetMinutes") Integer targetMinutes, @Param("targetDays") Integer targetDays);
}