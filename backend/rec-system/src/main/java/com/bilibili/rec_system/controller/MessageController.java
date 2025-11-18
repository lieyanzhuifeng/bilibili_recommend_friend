package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.entity.Message;
import com.bilibili.rec_system.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestParam Long senderId,
                                         @RequestParam Long receiverId,
                                         @RequestParam String content) {
        boolean success = messageService.sendMessage(senderId, receiverId, content);
        if (success) {
            return ResponseEntity.ok().body("消息发送成功");
        } else {
            return ResponseEntity.badRequest().body("消息发送失败");
        }
    }

    /**
     * 获取最近聊天记录
     */
    @GetMapping("/recent-chat")
    public ResponseEntity<List<Message>> getRecentChat(@RequestParam Long user1,
                                                       @RequestParam Long user2,
                                                       @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = messageService.getRecentChat(user1, user2, limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取增量消息（基于消息ID）
     */
    @GetMapping("/new-messages/id")
    public ResponseEntity<List<Message>> getNewMessagesById(@RequestParam Long userId,
                                                            @RequestParam Long lastMessageId) {
        List<Message> messages = messageService.getNewMessages(userId, lastMessageId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取增量消息（基于时间戳）
     */
    @GetMapping("/new-messages/time")
    public ResponseEntity<List<Message>> getNewMessagesByTime(@RequestParam Long userId,
                                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastTime) {
        List<Message> messages = messageService.getNewMessages(userId, lastTime);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取完整聊天记录
     */
    @GetMapping("/full-chat")
    public ResponseEntity<List<Message>> getFullChatHistory(@RequestParam Long user1,
                                                            @RequestParam Long user2) {
        List<Message> messages = messageService.getFullChatHistory(user1, user2);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(@RequestParam Long userId,
                                                      @RequestParam Long lastMessageId) {
        Long count = messageService.getUnreadMessageCount(userId, lastMessageId);
        return ResponseEntity.ok(count);
    }

    /**
     * 根据消息ID删除消息
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        boolean success = messageService.deleteMessage(messageId);
        if (success) {
            return ResponseEntity.ok().body("消息删除成功");
        } else {
            return ResponseEntity.badRequest().body("消息删除失败");
        }
    }

    /**
     * 删除发送者的所有消息
     */
    @DeleteMapping("/sender/{senderId}")
    public ResponseEntity<?> deleteMessagesBySender(@PathVariable Long senderId) {
        int deletedCount = messageService.deleteMessagesBySender(senderId);
        return ResponseEntity.ok().body("成功删除 " + deletedCount + " 条消息");
    }

    /**
     * 删除接收者的所有消息
     */
    @DeleteMapping("/receiver/{receiverId}")
    public ResponseEntity<?> deleteMessagesByReceiver(@PathVariable Long receiverId) {
        int deletedCount = messageService.deleteMessagesByReceiver(receiverId);
        return ResponseEntity.ok().body("成功删除 " + deletedCount + " 条消息");
    }

    /**
     * 删除两个用户之间的聊天记录
     */
    @DeleteMapping("/chat-history")
    public ResponseEntity<?> deleteChatHistory(@RequestParam Long user1,
                                               @RequestParam Long user2) {
        int deletedCount = messageService.deleteChatHistory(user1, user2);
        return ResponseEntity.ok().body("成功删除 " + deletedCount + " 条聊天记录");
    }

    /**
     * 获取发送者的所有消息
     */
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Message>> getMessagesBySender(@PathVariable Long senderId) {
        List<Message> messages = messageService.getMessagesBySender(senderId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取接收者的所有消息
     */
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Message>> getMessagesByReceiver(@PathVariable Long receiverId) {
        List<Message> messages = messageService.getMessagesByReceiver(receiverId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取时间段内的聊天记录
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<Message>> getMessagesBetweenUsers(@RequestParam Long user1,
                                                                 @RequestParam Long user2,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Message> messages = messageService.getMessagesBetweenUsers(user1, user2, startTime, endTime);
        return ResponseEntity.ok(messages);
    }

    /**
     * 统计发送者的消息数量
     */
    @GetMapping("/count/sender/{senderId}")
    public ResponseEntity<Long> getMessageCountBySender(@PathVariable Long senderId) {
        Long count = messageService.getMessageCountBySender(senderId);
        return ResponseEntity.ok(count);
    }

    /**
     * 统计接收者的消息数量
     */
    @GetMapping("/count/receiver/{receiverId}")
    public ResponseEntity<Long> getMessageCountByReceiver(@PathVariable Long receiverId) {
        Long count = messageService.getMessageCountByReceiver(receiverId);
        return ResponseEntity.ok(count);
    }

    /**
     * 统计总消息数量
     */
    @GetMapping("/count/total")
    public ResponseEntity<Long> getTotalMessageCount() {
        Long count = messageService.getTotalMessageCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 搜索用户消息内容
     */
    @GetMapping("/search/user")
    public ResponseEntity<List<Message>> searchMessagesByContent(@RequestParam Long userId,
                                                                 @RequestParam String keyword) {
        List<Message> messages = messageService.searchMessagesByContent(userId, keyword);
        return ResponseEntity.ok(messages);
    }

    /**
     * 搜索聊天记录内容
     */
    @GetMapping("/search/chat")
    public ResponseEntity<List<Message>> searchMessagesInChat(@RequestParam Long user1,
                                                              @RequestParam Long user2,
                                                              @RequestParam String keyword) {
        List<Message> messages = messageService.searchMessagesInChat(user1, user2, keyword);
        return ResponseEntity.ok(messages);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        boolean success = messageService.markMessageAsRead(messageId);
        if (success) {
            return ResponseEntity.ok().body("消息已标记为已读");
        } else {
            return ResponseEntity.badRequest().body("标记失败");
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllMessagesAsRead(@RequestParam Long userId,
                                                   @RequestParam Long lastMessageId) {
        int markedCount = messageService.markAllMessagesAsRead(userId, lastMessageId);
        return ResponseEntity.ok().body("成功标记 " + markedCount + " 条消息为已读");
    }

    /**
     * 获取用户的聊天伙伴列表
     */
    @GetMapping("/chat-partners/{userId}")
    public ResponseEntity<List<Long>> getChatPartners(@PathVariable Long userId) {
        List<Long> partners = messageService.getChatPartners(userId);
        return ResponseEntity.ok(partners);
    }
}