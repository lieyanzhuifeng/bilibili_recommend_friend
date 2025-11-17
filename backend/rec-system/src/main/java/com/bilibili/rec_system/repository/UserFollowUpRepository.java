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

    // 3.3 关注交集推荐
    @Query("SELECT ufu.upId FROM UserFollowUp ufu WHERE ufu.userId = :userId")
    List<Long> findFollowedUPsByUser(@Param("userId") Long userId);

    @Query("SELECT ufu.userId FROM UserFollowUp ufu WHERE ufu.upId IN :upIds AND ufu.userId <> :userId GROUP BY ufu.userId HAVING COUNT(ufu.upId) > 0")
    List<Long> findUsersWithCommonFollows(@Param("upIds") List<Long> upIds, @Param("userId") Long userId);

    // 3.7 关注时间缘分
    @Query("SELECT ufu FROM UserFollowUp ufu WHERE ufu.upId = :upId AND ufu.followTime = :followDate AND ufu.userId <> :userId")
    List<UserFollowUp> findUsersWithSameFollowDate(@Param("upId") Long upId, @Param("followDate") LocalDate followDate, @Param("userId") Long userId);

    @Query("SELECT ufu.followTime FROM UserFollowUp ufu WHERE ufu.userId = :userId AND ufu.upId = :upId")
    LocalDate findFollowTimeByUserAndUP(@Param("userId") Long userId, @Param("upId") Long upId);
}