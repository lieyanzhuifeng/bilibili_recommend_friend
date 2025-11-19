package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserTopTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTopThemeRepository extends JpaRepository<UserTopTheme, Long> {

    /**
     * 获取用户的前三主题（按比例降序）
     */
    @Query("SELECT utt FROM UserTopTheme utt WHERE utt.userId = :userId ORDER BY utt.proportion DESC")
    List<UserTopTheme> findTop3ByUserId(@Param("userId") Long userId);
}