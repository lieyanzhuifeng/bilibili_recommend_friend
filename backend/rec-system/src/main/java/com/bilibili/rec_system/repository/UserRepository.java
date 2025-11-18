package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 基础信息查询
    User findByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findByUserIds(@Param("userIds") List<Long> userIds);

    List<User> findByUserIdIn(List<Long> userIds);
}