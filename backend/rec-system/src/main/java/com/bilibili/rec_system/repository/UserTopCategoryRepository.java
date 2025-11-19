package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserTopCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTopCategoryRepository extends JpaRepository<UserTopCategory, Long> {

    /**
     * 获取用户的前三分区（按比例降序）
     */
    @Query("SELECT utc FROM UserTopCategory utc WHERE utc.userId = :userId ORDER BY utc.proportion DESC")
    List<UserTopCategory> findTop3ByUserId(@Param("userId") Long userId);
}