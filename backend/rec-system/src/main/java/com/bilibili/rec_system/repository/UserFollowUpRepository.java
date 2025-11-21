package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserFollowUpRepository extends JpaRepository<UserFollowUp, Long> {
    // 根据upID和userID和指定时间差范围查找用户
    @Query("SELECT ufu.userId FROM UserFollowUp ufu " +
            "WHERE ufu.upId = :upId " +
            "AND ufu.userId <> :currentUserId " +
            "AND ABS(DATEDIFF(ufu.followTime, (SELECT ufu2.followTime FROM UserFollowUp ufu2 WHERE ufu2.userId = :currentUserId AND ufu2.upId = :upId))) BETWEEN :minDays AND :maxDays")
    List<Long> findUsersWithFollowTimeInRange(@Param("currentUserId") Long currentUserId,
                                              @Param("upId") Long upId,
                                              @Param("minDays") Integer minDays,
                                              @Param("maxDays") Integer maxDays);


    // 根据userID和upID计算关注天数（从关注日期到现在的天数）
    @Query("SELECT DATEDIFF(CURRENT_DATE, ufu.followTime) FROM UserFollowUp ufu WHERE ufu.userId = :userId AND ufu.upId = :upId")
    Integer findFollowDaysByUserAndUP(@Param("userId") Long userId, @Param("upId") Long upId);

    // 查找关注了某个UP主的所有其他用户
    @Query("SELECT ufu.userId FROM UserFollowUp ufu WHERE ufu.upId = :upId AND ufu.userId <> :excludeUserId")
    List<Long> findUsersByUpIdExcludingUser(@Param("upId") Long upId, @Param("excludeUserId") Long excludeUserId);

    /**
     * 获取用户关注的所有UP主ID列表
     */
    @Query("SELECT ufu.upId FROM UserFollowUp ufu WHERE ufu.userId = :userId")
    List<Long> findUpIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户关注的UP主数量
     */
    @Query("SELECT COUNT(ufu) FROM UserFollowUp ufu WHERE ufu.userId = :userId")
    Long countFollowedUpsByUserId(@Param("userId") Long userId);

    /**
     * 获取两个用户共同关注的UP主ID列表
     */
    @Query("SELECT ufu1.upId FROM UserFollowUp ufu1 " +
            "JOIN UserFollowUp ufu2 ON ufu1.upId = ufu2.upId " +
            "WHERE ufu1.userId = :userId1 AND ufu2.userId = :userId2")
    List<Long> findCommonUpIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 获取所有关注了指定UP主的用户ID
     */
    @Query("SELECT ufu.userId FROM UserFollowUp ufu WHERE ufu.upId = :upId")
    List<Long> findUserIdsByUpId(@Param("upId") Long upId);

}