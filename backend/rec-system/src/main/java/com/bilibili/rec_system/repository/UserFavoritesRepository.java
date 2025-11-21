package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoritesRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * 获取用户收藏的所有视频ID
     */
    @Query("SELECT uf.videoId FROM UserFavorite uf WHERE uf.userId = :userId")
    List<Long> findVideoIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取两个用户共同收藏的视频ID列表
     */
    @Query("SELECT uf1.videoId FROM UserFavorite uf1 " +
            "JOIN UserFavorite uf2 ON uf1.videoId = uf2.videoId " +
            "WHERE uf1.userId = :userId1 AND uf2.userId = :userId2")
    List<Long> findCommonVideoIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}