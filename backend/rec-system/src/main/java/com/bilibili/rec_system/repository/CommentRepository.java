package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 3.1 推荐回复过自己评论的用户
    @Query("SELECT DISTINCT c.userId FROM Comment c WHERE c.parentId = :userId")
    List<Long> findUsersWhoRepliedToUser(@Param("userId") Long userId);

    // 3.2 推荐在同一视频下评论过的用户
    @Query("SELECT DISTINCT c2.userId FROM Comment c1 JOIN Comment c2 ON c1.videoId = c2.videoId WHERE c1.userId = :userId AND c2.userId <> :userId")
    List<Long> findCoCommentedUsers(@Param("userId") Long userId);

    @Query("SELECT c.videoId FROM Comment c WHERE c.userId = :userId")
    List<Long> findCommentedVideosByUser(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c.userId FROM Comment c WHERE c.videoId IN :videoIds AND c.userId <> :userId")
    List<Long> findUsersWhoCommentedOnVideos(@Param("videoIds") List<Long> videoIds, @Param("userId") Long userId);
}