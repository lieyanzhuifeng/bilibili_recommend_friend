package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    /**
     * 根据用户ID获取用户统计信息（userID是主键）
     */
    UserStatistics findByUserId(Long userId);

    /**
     * 获取所有其他用户的统计信息
     */
    @Query("SELECT us FROM UserStatistics us WHERE us.userId <> :excludeUserId")
    List<UserStatistics> findAllExcludingUser(@Param("excludeUserId") Long excludeUserId);
}