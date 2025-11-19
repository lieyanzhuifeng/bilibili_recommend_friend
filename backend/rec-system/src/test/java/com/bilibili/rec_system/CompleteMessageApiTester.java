package com.bilibili.rec_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompleteMessageApiTester {

    private static final String BASE_URL = "http://localhost:8080/api/messages";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 全局用户ID配置
    private static final Long TEST_SENDER_ID = 1L;
    private static final Long TEST_RECEIVER_ID = 2L;
    private static final Long TEST_USER_ID = 1L; // 用于单用户查询
    private static final Long TEST_OTHER_USER_ID = 3L; // 额外的测试用户

    // 测试消息ID（会在运行时更新）
    private static Long testMessageId = null;

    public static void main(String[] args) {
        CompleteMessageApiTester tester = new CompleteMessageApiTester();

        try {
            System.out.println("🚀 开始全面测试消息API...");
            System.out.println("📝 测试用户配置:");
            System.out.println("   发送者ID: " + TEST_SENDER_ID);
            System.out.println("   接收者ID: " + TEST_RECEIVER_ID);
            System.out.println("   当前用户ID: " + TEST_USER_ID);
            System.out.println("   其他用户ID: " + TEST_OTHER_USER_ID + "\n");

            // 第一阶段：基础功能测试
            tester.testBasicFunctions();

            // 第二阶段：搜索和统计测试
            tester.testSearchAndStatistics();

            // 第三阶段：消息状态管理测试
            tester.testMessageManagement();

            // 第四阶段：清理测试数据
            tester.cleanupTestData();

            System.out.println("🎉 所有API测试完成！");

        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 第一阶段：基础功能测试
     */
    public void testBasicFunctions() throws Exception {
        System.out.println("=== 第一阶段：基础功能测试 ===\n");

        // 1. 发送消息
        testSendMessage();

        // 2. 获取完整聊天记录
        testGetFullChatHistory();

        // 3. 获取最近聊天记录
        testGetRecentChat();

        // 4. 增量消息（基于ID）
        testGetNewMessagesById();

        // 5. 增量消息（基于时间）
        testGetNewMessagesByTime();

        // 6. 获取发送者的消息
        testGetMessagesBySender();

        // 7. 获取接收者的消息
        testGetMessagesByReceiver();

        // 8. 获取时间段内的聊天记录
        testGetMessagesBetweenUsers();
    }

    /**
     * 第二阶段：搜索和统计测试
     */
    public void testSearchAndStatistics() throws Exception {
        System.out.println("\n=== 第二阶段：搜索和统计测试 ===\n");

        // 9. 统计发送者的消息数量
        testGetMessageCountBySender();

        // 10. 统计接收者的消息数量
        testGetMessageCountByReceiver();

        // 11. 统计总消息数量
        testGetTotalMessageCount();

        // 12. 搜索用户消息内容
        testSearchMessagesByContent();

        // 13. 搜索聊天记录内容
        testSearchMessagesInChat();

        // 14. 获取聊天伙伴列表
        testGetChatPartners();
    }

    /**
     * 第三阶段：消息状态管理测试
     */
    public void testMessageManagement() throws Exception {
        System.out.println("\n=== 第三阶段：消息状态管理测试 ===\n");

        // 16. 获取未读消息数量
        testGetUnreadMessageCount();

        // 17. 标记消息为已读
        testMarkMessageAsRead();

        // 18. 标记所有消息为已读
        testMarkAllMessagesAsRead();

        // 注意：删除操作放在最后测试
    }

    /**
     * 第四阶段：清理测试数据
     */
    public void cleanupTestData() throws Exception {
        System.out.println("\n=== 第四阶段：清理测试数据 ===\n");

        // 19. 删除特定消息
        testDeleteMessage();

        // 20. 删除发送者的所有消息
        testDeleteMessagesBySender();

        // 21. 删除接收者的所有消息
        testDeleteMessagesByReceiver();

        // 22. 删除聊天记录
        testDeleteChatHistory();
    }

    // ========== 具体的测试方法实现 ==========

    public void testSendMessage() throws Exception {
        System.out.println("1. 📤 测试发送消息");
        String url = BASE_URL + "/send?senderId=" + TEST_SENDER_ID +
                "&receiverId=" + TEST_RECEIVER_ID +
                "&content=你好，这是一条测试消息" + System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("发送消息", response);
    }

    public void testGetFullChatHistory() throws Exception {
        System.out.println("2. 📋 测试获取完整聊天记录");
        String url = BASE_URL + "/full-chat?user1=" + TEST_SENDER_ID + "&user2=" + TEST_RECEIVER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("完整聊天记录", response);
    }

    public void testGetRecentChat() throws Exception {
        System.out.println("3. 💬 测试获取最近聊天记录");
        String url = BASE_URL + "/recent-chat?user1=" + TEST_SENDER_ID +
                "&user2=" + TEST_RECEIVER_ID + "&limit=5";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("最近聊天记录", response);
    }

    public void testGetNewMessagesById() throws Exception {
        System.out.println("4. 🔄 测试增量消息（基于ID）");
        String url = BASE_URL + "/new-messages/id?userId=" + TEST_USER_ID + "&lastMessageId=0";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("增量消息(ID)", response);
    }

    public void testGetNewMessagesByTime() throws Exception {
        System.out.println("5. ⏰ 测试增量消息（基于时间）");
        String time = LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String url = BASE_URL + "/new-messages/time?userId=" + TEST_USER_ID + "&lastTime=" + time;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("增量消息(时间)", response);
    }

    public void testGetMessagesBySender() throws Exception {
        System.out.println("6. 📨 测试获取发送者的消息");
        String url = BASE_URL + "/sender/" + TEST_SENDER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("发送者消息", response);
    }

    public void testGetMessagesByReceiver() throws Exception {
        System.out.println("7. 📥 测试获取接收者的消息");
        String url = BASE_URL + "/receiver/" + TEST_RECEIVER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("接收者消息", response);
    }

    public void testGetMessagesBetweenUsers() throws Exception {
        System.out.println("8. 📅 测试获取时间段内的聊天记录");
        String startTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String url = BASE_URL + "/time-range?user1=" + TEST_SENDER_ID +
                "&user2=" + TEST_RECEIVER_ID +
                "&startTime=" + startTime + "&endTime=" + endTime;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("时间段聊天记录", response);
    }

    public void testGetMessageCountBySender() throws Exception {
        System.out.println("9. 🔢 测试统计发送者的消息数量");
        String url = BASE_URL + "/count/sender/" + TEST_SENDER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("发送者消息数量", response);
    }

    public void testGetMessageCountByReceiver() throws Exception {
        System.out.println("10. 🔢 测试统计接收者的消息数量");
        String url = BASE_URL + "/count/receiver/" + TEST_RECEIVER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("接收者消息数量", response);
    }

    public void testGetTotalMessageCount() throws Exception {
        System.out.println("11. 📊 测试统计总消息数量");
        String url = BASE_URL + "/count/total";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("总消息数量", response);
    }

    public void testSearchMessagesByContent() throws Exception {
        System.out.println("12. 🔍 测试搜索用户消息内容");
        String url = BASE_URL + "/search/user?userId=" + TEST_USER_ID + "&keyword=测试";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("搜索用户消息", response);
    }

    public void testSearchMessagesInChat() throws Exception {
        System.out.println("13. 🔍 测试搜索聊天记录内容");
        String url = BASE_URL + "/search/chat?user1=" + TEST_SENDER_ID +
                "&user2=" + TEST_RECEIVER_ID + "&keyword=测试";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("搜索聊天记录", response);
    }

    public void testGetChatPartners() throws Exception {
        System.out.println("14. 👥 测试获取聊天伙伴列表");
        String url = BASE_URL + "/chat-partners/" + TEST_USER_ID;
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("聊天伙伴列表", response);
    }

    public void testGetRecentChatsWithPreview() throws Exception {
        System.out.println("15. 💭 测试获取最近聊天列表及预览");
        String url = BASE_URL + "/recent-chats/" + TEST_USER_ID + "?limit=5";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("最近聊天预览", response);
    }

    public void testGetUnreadMessageCount() throws Exception {
        System.out.println("16. 📬 测试获取未读消息数量");
        String url = BASE_URL + "/unread-count?userId=" + TEST_USER_ID + "&lastMessageId=0";
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("未读消息数量", response);
    }

    public void testMarkMessageAsRead() throws Exception {
        System.out.println("17. ✅ 测试标记消息为已读");
        // 这里需要先获取一个真实的消息ID，这里用示例ID
        String url = BASE_URL + "/1/read";
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("标记消息已读", response);
    }

    public void testMarkAllMessagesAsRead() throws Exception {
        System.out.println("18. ✅ 测试标记所有消息为已读");
        String url = BASE_URL + "/mark-all-read?userId=" + TEST_USER_ID + "&lastMessageId=0";
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("标记所有消息已读", response);
    }

    public void testDeleteMessage() throws Exception {
        System.out.println("19. 🗑️ 测试删除消息");
        // 这里需要先获取一个真实的消息ID，这里用示例ID
        String url = BASE_URL + "/1";
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("删除消息", response);
    }

    public void testDeleteMessagesBySender() throws Exception {
        System.out.println("20. 🗑️ 测试删除发送者的所有消息");
        String url = BASE_URL + "/sender/" + TEST_SENDER_ID;
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("删除发送者消息", response);
    }

    public void testDeleteMessagesByReceiver() throws Exception {
        System.out.println("21. 🗑️ 测试删除接收者的所有消息");
        String url = BASE_URL + "/receiver/" + TEST_RECEIVER_ID;
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("删除接收者消息", response);
    }

    public void testDeleteChatHistory() throws Exception {
        System.out.println("22. 🗑️ 测试删除聊天记录");
        String url = BASE_URL + "/chat-history?user1=" + TEST_SENDER_ID + "&user2=" + TEST_RECEIVER_ID;
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("删除聊天记录", response);
    }

    /**
     * 格式化并打印响应
     */
    private void printResponse(String apiName, HttpResponse<String> response) {
        System.out.println("📡 API: " + apiName);
        System.out.println("📊 状态码: " + response.statusCode());
        System.out.println("📄 响应内容:");

        try {
            String json = response.body();
            if (json != null && !json.trim().isEmpty()) {
                Object jsonObject = objectMapper.readValue(json, Object.class);
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
                System.out.println(prettyJson);
            } else {
                System.out.println("(空响应)");
            }
        } catch (Exception e) {
            System.out.println(response.body());
        }
        System.out.println("─".repeat(50));
    }

    /**
     * 获取全局用户ID的方法（便于外部访问）
     */
    public static Long getTestSenderId() {
        return TEST_SENDER_ID;
    }

    public static Long getTestReceiverId() {
        return TEST_RECEIVER_ID;
    }

    public static Long getTestUserId() {
        return TEST_USER_ID;
    }

    public static Long getTestOtherUserId() {
        return TEST_OTHER_USER_ID;
    }
}