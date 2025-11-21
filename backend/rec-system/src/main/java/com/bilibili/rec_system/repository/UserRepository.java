package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 基础信息查询
    User findByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findByUserIds(@Param("userIds") List<Long> userIds);

    List<User> findByUserIdIn(List<Long> userIds);

    /**
     * 根据用户ID列表获取用户名映射
     */
    @Query("SELECT new map(u.userId as userId, u.username as username) FROM User u WHERE u.userId IN :userIds")
    List<Map<String, Object>> findUsernamesByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 获取UP主名称（UP主也是用户）
     */
    @Query("SELECT u.username FROM User u WHERE u.userId = :upId")
    String findUpNameById(@Param("upId") Long upId);

    /**
     * 批量获取UP主名称
     */
    @Query("SELECT u.userId, u.username FROM User u WHERE u.userId IN :upIds")
    List<Object[]> findUpNamesByIds(@Param("upIds") List<Long> upIds);
}