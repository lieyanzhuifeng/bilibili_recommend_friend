package com.bilibili.rec_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class FriendApiTester {

    private static final String BASE_URL = "http://localhost:8080/api/friends";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 测试用户配置
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TARGET_USER_ID = 2L;
    private static final Long TEST_OTHER_USER_ID = 3L;

    public static void main(String[] args) {
        FriendApiTester tester = new FriendApiTester();

        try {
            System.out.println("🚀 开始测试好友系统API...");
            System.out.println("📝 测试用户配置:");
            System.out.println("   当前用户ID: " + TEST_USER_ID);
            System.out.println("   目标用户ID: " + TEST_TARGET_USER_ID);
            System.out.println("   其他用户ID: " + TEST_OTHER_USER_ID + "\n");

            // 测试流程
            tester.testFriendWorkflow();

            System.out.println("🎉 好友API测试完成！");

        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 完整的好友工作流测试
     */
    public void testFriendWorkflow() throws Exception {
        System.out.println("=== 好友系统完整工作流测试 ===\n");

        // 1. 发送好友申请
        testSendFriendRequest();

        // 2. 检查好友申请列表
        testGetPendingFriendRequests();

        // 3. 检查好友关系（应该为false）
        testIsFriend(false);

        // 4. 接受好友申请
        testAcceptFriendRequest();

        // 5. 再次检查好友关系（应该为true）
        testIsFriend(true);

        // 6. 获取好友列表
        testGetFriends();

        // 7. 测试拒绝好友申请（与其他用户）
        testRejectFriendRequest();

        // 8. 统计功能测试
        testStatistics();

        // 9. 清理测试数据（删除好友）
        testRemoveFriend();
    }

    public void testSendFriendRequest() throws Exception {
        System.out.println("1. 📤 测试发送好友申请");
        String url = BASE_URL + "/request?userId=" + TEST_USER_ID + "&targetUserId=" + TEST_TARGET_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("发送好友申请", response);
    }

    public void testGetPendingFriendRequests() throws Exception {
        System.out.println("2. 📬 测试获取待处理好友申请");
        String url = BASE_URL + "/requests/" + TEST_TARGET_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("待处理好友申请", response);
    }

    public void testIsFriend(boolean expected) throws Exception {
        System.out.println("3. 🤝 测试检查好友关系");
        String url = BASE_URL + "/check?userId=" + TEST_USER_ID + "&targetUserId=" + TEST_TARGET_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("检查好友关系", response);

        // 验证结果是否符合预期
        boolean actual = Boolean.parseBoolean(response.body().trim());
        if (actual == expected) {
            System.out.println("✅ 好友关系检查正确: " + actual);
        } else {
            System.out.println("❌ 好友关系检查错误，预期: " + expected + "，实际: " + actual);
        }
    }

    public void testAcceptFriendRequest() throws Exception {
        System.out.println("4. ✅ 测试接受好友申请");
        String url = BASE_URL + "/accept?userId=" + TEST_TARGET_USER_ID + "&requesterId=" + TEST_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("接受好友申请", response);
    }

    public void testGetFriends() throws Exception {
        System.out.println("5. 👥 测试获取好友列表");
        String url = BASE_URL + "/list/" + TEST_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("好友列表", response);
    }

    public void testRejectFriendRequest() throws Exception {
        System.out.println("6. ❌ 测试拒绝好友申请");

        // 先发送一个申请
        String sendUrl = BASE_URL + "/request?userId=" + TEST_OTHER_USER_ID + "&targetUserId=" + TEST_USER_ID;
        HttpRequest sendRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(sendUrl))
                .build();
        httpClient.send(sendRequest, HttpResponse.BodyHandlers.ofString());

        // 然后拒绝这个申请
        String rejectUrl = BASE_URL + "/reject?userId=" + TEST_USER_ID + "&requesterId=" + TEST_OTHER_USER_ID;
        HttpRequest rejectRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(rejectUrl))
                .build();

        HttpResponse<String> response = httpClient.send(rejectRequest, HttpResponse.BodyHandlers.ofString());
        printResponse("拒绝好友申请", response);
    }

    public void testStatistics() throws Exception {
        System.out.println("7. 📊 测试统计功能");

        // 好友数量
        String countUrl = BASE_URL + "/count/" + TEST_USER_ID;
        HttpRequest countRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(countUrl))
                .build();
        HttpResponse<String> countResponse = httpClient.send(countRequest, HttpResponse.BodyHandlers.ofString());
        printResponse("好友数量", countResponse);

        // 待处理申请数量
        String requestCountUrl = BASE_URL + "/requests/count/" + TEST_USER_ID;
        HttpRequest requestCountRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(requestCountUrl))
                .build();
        HttpResponse<String> requestCountResponse = httpClient.send(requestCountRequest, HttpResponse.BodyHandlers.ofString());
        printResponse("待处理申请数量", requestCountResponse);
    }

    public void testRemoveFriend() throws Exception {
        System.out.println("8. 🗑️ 测试删除好友");
        String url = BASE_URL + "/remove?userId=" + TEST_USER_ID + "&targetUserId=" + TEST_TARGET_USER_ID;

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        printResponse("删除好友", response);

        // 验证删除后好友关系
        System.out.println("9. 🔍 验证删除结果");
        testIsFriend(false);
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
                // 尝试美化 JSON 输出
                Object jsonObject = objectMapper.readValue(json, Object.class);
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
                System.out.println(prettyJson);
            } else {
                System.out.println("(空响应)");
            }
        } catch (Exception e) {
            // 如果不是 JSON 格式，直接输出
            System.out.println(response.body());
        }
        System.out.println("─".repeat(50));
    }
}