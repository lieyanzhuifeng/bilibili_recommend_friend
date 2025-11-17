package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** 获取两个用户之间的聊天记录 */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)) " +
            "ORDER BY m.sendTime ASC")
    List<Message> findChatBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    /** 获取两个用户之间最近N条聊天记录 */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)) " +
            "ORDER BY m.sendTime DESC LIMIT :limit")
    List<Message> findRecentChatBetweenUsers(@Param("user1") Long user1,
                                             @Param("user2") Long user2,
                                             @Param("limit") int limit);

    /** 获取用户的新消息 */
    @Query("SELECT m FROM Message m WHERE " +
            "m.receiverId = :userId AND m.messageId > :lastMessageId " +
            "ORDER BY m.sendTime ASC")
    List<Message> findNewMessages(@Param("userId") Long userId,
                                  @Param("lastMessageId") Long lastMessageId);

    /** 获取用户的新消息（基于时间戳） */
    @Query("SELECT m FROM Message m WHERE " +
            "m.receiverId = :userId AND m.sendTime > :lastTime " +
            "ORDER BY m.sendTime ASC")
    List<Message> findNewMessagesByTime(@Param("userId") Long userId,
                                        @Param("lastTime") LocalDateTime lastTime);

    /** 获取用户的所有未读消息数量 */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.messageId > :lastMessageId")
    Long countUnreadMessages(@Param("userId") Long userId, @Param("lastMessageId") Long lastMessageId);

    /** 根据消息ID删除消息 */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.messageId = :messageId")
    int deleteByMessageId(@Param("messageId") Long messageId);

    /** 根据发送者ID删除消息 */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.senderId = :senderId")
    int deleteBySenderId(@Param("senderId") Long senderId);

    /** 根据接收者ID删除消息 */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.receiverId = :receiverId")
    int deleteByReceiverId(@Param("receiverId") Long receiverId);

    /** 删除两个用户之间的所有聊天记录 */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE " +
            "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)")
    int deleteChatHistory(@Param("user1") Long user1, @Param("user2") Long user2);

    /** 根据发送者ID查询消息 */
    List<Message> findBySenderId(Long senderId);

    /** 根据接收者ID查询消息 */
    List<Message> findByReceiverId(Long receiverId);

    /** 查询时间段内的聊天记录 */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)) AND " +
            "m.sendTime BETWEEN :startTime AND :endTime " +
            "ORDER BY m.sendTime ASC")
    List<Message> findMessagesBetweenUsersInTimeRange(@Param("user1") Long user1,
                                                      @Param("user2") Long user2,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /** 统计发送者消息数量 */
    Long countBySenderId(Long senderId);

    /** 统计接收者消息数量 */
    Long countByReceiverId(Long receiverId);

    /** 搜索消息内容 */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :userId OR m.receiverId = :userId) AND " +
            "m.content LIKE %:keyword% " +
            "ORDER BY m.sendTime DESC")
    List<Message> searchByContentAndUser(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索聊天记录内容 */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)) AND " +
            "m.content LIKE %:keyword% " +
            "ORDER BY m.sendTime DESC")
    List<Message> searchByContentInChat(@Param("user1") Long user1,
                                        @Param("user2") Long user2,
                                        @Param("keyword") String keyword);

    /** 获取用户的聊天伙伴 */
    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END " +
            "FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findChatPartnersByUserId(@Param("userId") Long userId);

    /** 获取最近聊天列表及最后消息预览 */
    @Query("SELECT partnerId, lastMessage, lastTime FROM (" +
            "SELECT " +
            "CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END as partnerId, " +
            "m.content as lastMessage, " +
            "MAX(m.sendTime) as lastTime " +
            "FROM Message m " +
            "WHERE m.senderId = :userId OR m.receiverId = :userId " +
            "GROUP BY partnerId " +
            "ORDER BY lastTime DESC LIMIT :limit" +
            ")")
    List<Object[]> findRecentChatsWithPreview(@Param("userId") Long userId, @Param("limit") int limit);

    /** 统计总消息数量 */
    @Query("SELECT COUNT(m) FROM Message m")
    Long countTotalMessages();

    /** 获取用户发送的最后一条消息 */
    @Query("SELECT m FROM Message m WHERE m.senderId = :userId ORDER BY m.sendTime DESC LIMIT 1")
    Message findLastMessageBySender(@Param("userId") Long userId);

    /** 获取用户接收的最后一条消息 */
    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId ORDER BY m.sendTime DESC LIMIT 1")
    Message findLastMessageByReceiver(@Param("userId") Long userId);
}