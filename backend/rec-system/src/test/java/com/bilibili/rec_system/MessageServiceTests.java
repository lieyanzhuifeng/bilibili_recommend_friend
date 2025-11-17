package com.bilibili.rec_system;

import com.bilibili.rec_system.entity.Message;
import com.bilibili.rec_system.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MessageServiceTests {

    @Autowired
    private MessageService messageService;

    private Long user1Id = 1L; // 高冷的嘲笑
    private Long user2Id = 2L; // 熬夜冠军
    private Long user3Id = 3L; // 快乐摸鱼人

    @BeforeEach
    void setUp() {
        System.out.println("=== 测试开始，清理测试数据 ===");
        // 清理测试数据
        messageService.deleteChatHistory(user1Id, user2Id);
        messageService.deleteChatHistory(user1Id, user3Id);
        messageService.deleteChatHistory(user2Id, user3Id);
        printMessageTable("清理后的message表");
    }

    // 打印完整message表的工具方法
    private void printMessageTable(String title) {
        System.out.println("\n=== " + title + " ===");
        System.out.println("messageID | senderID | receiverID | content                | sendTime");
        System.out.println("----------|----------|------------|------------------------|------------");

        List<Message> allMessages = messageService.getFullChatHistory(user1Id, user2Id);
        allMessages.addAll(messageService.getFullChatHistory(user1Id, user3Id));
        allMessages.addAll(messageService.getFullChatHistory(user2Id, user3Id));

        // 去重并排序
        allMessages = allMessages.stream()
                .distinct()
                .sorted((m1, m2) -> m1.getMessageId().compareTo(m2.getMessageId()))
                .toList();

        if (allMessages.isEmpty()) {
            System.out.println("(空表)");
        } else {
            for (Message message : allMessages) {
                System.out.printf("%-9d | %-8d | %-10d | %-22s | %s%n",
                        message.getMessageId(),
                        message.getSenderId(),
                        message.getReceiverId(),
                        truncateContent(message.getContent(), 20),
                        message.getSendTime());
            }
        }

        // 统计信息
        long totalCount = allMessages.size();
        long user1Sent = messageService.getMessageCountBySender(user1Id);
        long user2Sent = messageService.getMessageCountBySender(user2Id);
        long user3Sent = messageService.getMessageCountBySender(user3Id);

        System.out.println("统计信息:");
        System.out.println("总消息数: " + totalCount);
        System.out.println("用户1发送: " + user1Sent);
        System.out.println("用户2发送: " + user2Sent);
        System.out.println("用户3发送: " + user3Sent);
        System.out.println("=== " + title + " 结束 ===\n");
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength - 3) + "...";
    }

    @Test
    void testSendMessage() {
        System.out.println("=== 测试发送消息 ===");

        printMessageTable("发送消息前的message表");

        boolean result = messageService.sendMessage(user1Id, user2Id, "你好，我是用户1，很高兴认识你！");
        assertTrue(result, "发送消息应该成功");

        printMessageTable("发送消息后的message表");

        System.out.println("✅ 发送消息测试成功");
    }

    @Test
    void testGetRecentChat() {
        System.out.println("=== 测试获取最近聊天记录 ===");

        printMessageTable("初始message表");

        // 发送测试消息
        messageService.sendMessage(user1Id, user2Id, "第一条消息：你好用户2");
        printMessageTable("发送第一条消息后的message表");

        messageService.sendMessage(user2Id, user1Id, "第二条消息：你好用户1，收到你的消息");
        printMessageTable("发送第二条消息后的message表");

        messageService.sendMessage(user1Id, user2Id, "第三条消息：我们可以做朋友吗？");
        printMessageTable("发送第三条消息后的message表");

        List<Message> messages = messageService.getRecentChat(user1Id, user2Id, 2);
        assertEquals(2, messages.size(), "应该返回2条消息");

        System.out.println("获取到的最近2条聊天记录:");
        messages.forEach(msg ->
                System.out.println("  " + msg.getMessageId() + ": " +
                        msg.getSenderId() + " -> " + msg.getReceiverId() + ": " + msg.getContent())
        );

        System.out.println("✅ 获取最近聊天记录测试成功");
    }

    @Test
    void testGetNewMessages() {
        System.out.println("=== 测试获取增量消息 ===");

        printMessageTable("初始message表");

        // 发送第一条消息
        messageService.sendMessage(user1Id, user2Id, "这是第一条消息");
        printMessageTable("发送第一条消息后的message表");

        // 获取第一条消息的ID
        List<Message> firstMessages = messageService.getRecentChat(user1Id, user2Id, 1);
        Long firstMessageId = firstMessages.get(0).getMessageId();
        System.out.println("第一条消息ID: " + firstMessageId);

        // 发送第二条消息
        messageService.sendMessage(user1Id, user2Id, "这是第二条新消息");
        printMessageTable("发送第二条消息后的message表");

        // 获取增量消息
        List<Message> newMessages = messageService.getNewMessages(user2Id, firstMessageId);
        assertEquals(1, newMessages.size(), "应该只有1条新消息");
        assertEquals("这是第二条新消息", newMessages.get(0).getContent());

        System.out.println("获取到的增量消息:");
        newMessages.forEach(msg ->
                System.out.println("  " + msg.getMessageId() + ": " + msg.getContent())
        );

        System.out.println("✅ 获取增量消息测试成功");
    }

    @Test
    void testDeleteMessage() {
        System.out.println("=== 测试删除消息 ===");

        printMessageTable("初始message表");

        messageService.sendMessage(user1Id, user2Id, "这是一条待删除的测试消息，内容比较长以便观察");
        printMessageTable("发送消息后的message表");

        List<Message> messages = messageService.getRecentChat(user1Id, user2Id, 1);
        Long messageId = messages.get(0).getMessageId();
        System.out.println("要删除的消息ID: " + messageId);

        boolean deleteResult = messageService.deleteMessage(messageId);
        assertTrue(deleteResult, "删除消息应该成功");

        printMessageTable("删除消息后的message表");

        System.out.println("✅ 删除消息测试成功");
    }

    @Test
    void testDeleteChatHistory() {
        System.out.println("=== 测试删除聊天记录 ===");

        printMessageTable("初始message表");

        messageService.sendMessage(user1Id, user2Id, "用户1发给用户2的消息1");
        messageService.sendMessage(user2Id, user1Id, "用户2回复用户1的消息2");
        messageService.sendMessage(user1Id, user2Id, "用户1再次发送的消息3");
        printMessageTable("发送多条消息后的message表");

        int deletedCount = messageService.deleteChatHistory(user1Id, user2Id);
        assertTrue(deletedCount > 0, "应该删除了一些消息");

        printMessageTable("删除聊天记录后的message表");

        List<Message> history = messageService.getFullChatHistory(user1Id, user2Id);
        assertTrue(history.isEmpty(), "聊天记录应该为空");

        System.out.println("删除了 " + deletedCount + " 条消息");
        System.out.println("✅ 删除聊天记录测试成功");
    }

    @Test
    void testMultipleUsersChat() {
        System.out.println("=== 测试多用户聊天 ===");

        printMessageTable("初始message表");

        // 用户1与用户2聊天
        messageService.sendMessage(user1Id, user2Id, "用户1对用户2说：你好");
        printMessageTable("用户1->用户2发送消息后的message表");

        messageService.sendMessage(user2Id, user1Id, "用户2回复用户1：收到");
        printMessageTable("用户2->用户1回复后的message表");

        // 用户1与用户3聊天
        messageService.sendMessage(user1Id, user3Id, "用户1对用户3说：你好用户3");
        printMessageTable("用户1->用户3发送消息后的message表");

        messageService.sendMessage(user3Id, user1Id, "用户3回复用户1：你好用户1");
        printMessageTable("用户3->用户1回复后的message表");

        // 用户2与用户3聊天
        messageService.sendMessage(user2Id, user3Id, "用户2对用户3说：我们也是朋友");
        printMessageTable("用户2->用户3发送消息后的message表");

        // 查看各用户的聊天伙伴
        List<Long> user1Partners = messageService.getChatPartners(user1Id);
        List<Long> user2Partners = messageService.getChatPartners(user2Id);
        List<Long> user3Partners = messageService.getChatPartners(user3Id);

        System.out.println("用户1的聊天伙伴: " + user1Partners);
        System.out.println("用户2的聊天伙伴: " + user2Partners);
        System.out.println("用户3的聊天伙伴: " + user3Partners);

        System.out.println("✅ 多用户聊天测试成功");
    }

    @Test
    void testCompleteMessageWorkflow() {
        System.out.println("=== 测试完整消息工作流 ===");

        printMessageTable("工作流开始前的message表");

        // 阶段1: 建立聊天
        System.out.println("阶段1: 建立聊天");
        messageService.sendMessage(user1Id, user2Id, "你好，我们可以聊天吗？");
        printMessageTable("建立聊天后的message表");

        messageService.sendMessage(user2Id, user1Id, "当然可以，很高兴认识你！");
        printMessageTable("回复消息后的message表");

        // 阶段2: 搜索消息
        System.out.println("阶段2: 搜索消息");
        List<Message> searchResults = messageService.searchMessagesByContent(user1Id, "聊天");
        System.out.println("搜索到 " + searchResults.size() + " 条包含'聊天'的消息");

        // 阶段3: 获取统计信息
        System.out.println("阶段3: 获取统计信息");
        Long user1Sent = messageService.getMessageCountBySender(user1Id);
        Long user2Sent = messageService.getMessageCountBySender(user2Id);
        System.out.println("用户1发送了 " + user1Sent + " 条消息");
        System.out.println("用户2发送了 " + user2Sent + " 条消息");

        // 阶段4: 删除单条消息
        System.out.println("阶段4: 删除单条消息");
        List<Message> recent = messageService.getRecentChat(user1Id, user2Id, 1);
        if (!recent.isEmpty()) {
            Long lastMessageId = recent.get(0).getMessageId();
            messageService.deleteMessage(lastMessageId);
            printMessageTable("删除最后一条消息后的message表");
        }

        // 阶段5: 清空聊天记录
        System.out.println("阶段5: 清空聊天记录");
        int deleted = messageService.deleteChatHistory(user1Id, user2Id);
        printMessageTable("清空聊天记录后的message表");

        System.out.println("总共删除了 " + deleted + " 条消息");
        System.out.println("✅ 完整消息工作流测试成功");
    }
}