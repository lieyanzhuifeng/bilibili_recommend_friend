package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.Message;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {

    /** 发送消息 */
    boolean sendMessage(Long senderId, Long receiverId, String content);

    /** 获取最近聊天记录 */
    List<Message> getRecentChat(Long user1, Long user2, int limit);

    /** 获取增量消息（基于消息ID） */
    List<Message> getNewMessages(Long userId, Long lastMessageId);

    /** 获取增量消息（基于时间戳） */
    List<Message> getNewMessages(Long userId, LocalDateTime lastTime);

    /** 获取完整聊天记录 */
    List<Message> getFullChatHistory(Long user1, Long user2);

    /** 获取未读消息数量 */
    Long getUnreadMessageCount(Long userId, Long lastMessageId);

    /** 根据消息ID删除消息 */
    boolean deleteMessage(Long messageId);

    /** 删除发送者的所有消息 */
    int deleteMessagesBySender(Long senderId);

    /** 删除接收者的所有消息 */
    int deleteMessagesByReceiver(Long receiverId);

    /** 删除两个用户之间的聊天记录 */
    int deleteChatHistory(Long user1, Long user2);

    /** 获取发送者的所有消息 */
    List<Message> getMessagesBySender(Long senderId);

    /** 获取接收者的所有消息 */
    List<Message> getMessagesByReceiver(Long receiverId);

    /** 获取时间段内的聊天记录 */
    List<Message> getMessagesBetweenUsers(Long user1, Long user2, LocalDateTime startTime, LocalDateTime endTime);

    /** 统计发送者的消息数量 */
    Long getMessageCountBySender(Long senderId);

    /** 统计接收者的消息数量 */
    Long getMessageCountByReceiver(Long receiverId);

    /** 统计总消息数量 */
    Long getTotalMessageCount();

    /** 搜索用户消息内容 */
    List<Message> searchMessagesByContent(Long userId, String keyword);

    /** 搜索聊天记录内容 */
    List<Message> searchMessagesInChat(Long user1, Long user2, String keyword);

    /** 标记消息为已读 */
    boolean markMessageAsRead(Long messageId);

    /** 标记所有消息为已读 */
    int markAllMessagesAsRead(Long userId, Long lastMessageId);

    /** 获取用户的聊天伙伴列表 */
    List<Long> getChatPartners(Long userId);

    /** 获取最近聊天列表及预览 */
    List<Object[]> getRecentChatsWithPreview(Long userId, int limit);
}