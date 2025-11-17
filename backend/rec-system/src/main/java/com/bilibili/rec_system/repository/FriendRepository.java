package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 1. 好友申请相关
    @Query("SELECT f FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId")
    Optional<Friend> findByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId AND f.status = 'pending'")
    Optional<Friend> findPendingRequest(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friend f WHERE f.friendId = :userId AND f.status = 'pending'")
    List<Friend> findPendingRequestsToUser(@Param("userId") Long userId);

    // 修改：使用 'accepted' 而不是 'friend'
    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 'accepted'")
    List<Friend> findAllFriends(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Friend f WHERE (f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)")
    void deleteFriendRelationship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 修改：使用 'accepted' 而不是 'friend'
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE ((f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)) AND f.status = 'accepted'")
    boolean isFriend(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT f FROM Friend f")
    List<Friend> findAllFriends();
}