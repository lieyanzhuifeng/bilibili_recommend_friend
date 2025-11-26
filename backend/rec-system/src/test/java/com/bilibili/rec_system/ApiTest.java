package com.bilibili.rec_system;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ApiTest {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) {
        System.out.println("=== B站推荐系统 API 测试 ===\n");

        // 测试用户搜索API
        testUserSearchAPI();

        // 测试用户详情API
        testUserDetailAPI();

        // 测试视频搜索API
        testVideoSearchAPI();

        // 测试标签搜索API
        testTagSearchAPI();
    }

    /**
     * 测试用户搜索API
     */
    private static void testUserSearchAPI() {
        System.out.println("1. 测试用户搜索API");

        // 测试用例
        String[] testKeywords = {"人"};

        for (String keyword : testKeywords) {
            String apiUrl = "http://localhost:8080/api/users/search?keyword=" + keyword;
            System.out.println("\n搜索关键词: '" + keyword + "'");
            testGetRequest(apiUrl);
        }
    }

    /**
     * 测试用户详情API
     */
    private static void testUserDetailAPI() {
        System.out.println("\n\n2. 测试用户详情API");

        // 测试存在的用户
        testGetRequest("http://localhost:8080/api/users/1");
        testGetRequest("http://localhost:8080/api/users/2");

        // 测试不存在的用户
        testGetRequest("http://localhost:8080/api/users/99999");
    }

    /**
     * 测试视频搜索API
     */
    private static void testVideoSearchAPI() {
        System.out.println("\n\n3. 测试视频搜索API");

        // 测试用例 - 各种类型的视频标题关键词
        String[] testKeywords = {
                "徐静雨",
        };

        for (String keyword : testKeywords) {
            String apiUrl = "http://localhost:8080/api/users/videos/search?keyword=" +
                    java.net.URLEncoder.encode(keyword);
            System.out.println("\n搜索视频关键词: '" + keyword + "'");
            testGetRequest(apiUrl);

            // 添加短暂延迟，避免请求过于频繁
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 测试边界情况
        System.out.println("\n\n4. 测试视频搜索边界情况");

        // 测试空关键词
        testGetRequest("http://localhost:8080/api/users/videos/search?keyword=");

        // 测试特殊字符
        testGetRequest("http://localhost:8080/api/users/videos/search?keyword=" +
                java.net.URLEncoder.encode("#编程@"));

        // 测试长关键词
        String longKeyword = "这是一个非常长的搜索关键词用来测试系统的处理能力";
        testGetRequest("http://localhost:8080/api/users/videos/search?keyword=" +
                java.net.URLEncoder.encode(longKeyword));
    }

    /**
     * 测试标签搜索API
     */
    private static void testTagSearchAPI() {
        System.out.println("\n\n5. 测试标签搜索API");

        // 测试用例 - 各种类型的标签关键词
        String[] testKeywords = {
                "系列"
        };

        for (String keyword : testKeywords) {
            String apiUrl = "http://localhost:8080/api/users/tags/search?keyword=" +
                    java.net.URLEncoder.encode(keyword);
            System.out.println("\n搜索标签关键词: '" + keyword + "'");
            testGetRequest(apiUrl);

            // 添加短暂延迟，避免请求过于频繁
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 测试边界情况
        System.out.println("\n\n6. 测试标签搜索边界情况");

        // 测试空关键词
        System.out.println("\n测试空关键词:");
        testGetRequest("http://localhost:8080/api/users/tags/search?keyword=");

        // 测试特殊字符
        System.out.println("\n测试特殊字符:");
        testGetRequest("http://localhost:8080/api/users/tags/search?keyword=" +
                java.net.URLEncoder.encode("#标签@"));

        // 测试不存在的标签
        System.out.println("\n测试不存在的标签:");
        String nonExistentTag = "这个标签肯定不存在123456";
        testGetRequest("http://localhost:8080/api/users/tags/search?keyword=" +
                java.net.URLEncoder.encode(nonExistentTag));

        // 测试单个字符
        System.out.println("\n测试单个字符:");
        testGetRequest("http://localhost:8080/api/users/tags/search?keyword=系");

        // 测试精确匹配
        System.out.println("\n测试精确匹配:");
        testGetRequest("http://localhost:8080/api/users/tags/search?keyword=游戏系列");
    }

    /**
     * 执行GET请求测试
     */
    private static void testGetRequest(String apiUrl) {
        try {
            System.out.println("\n--- 测试请求: " + apiUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "Bilibili-API-Test/1.0")
                    .GET()
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            // 打印响应信息
            printResponseInfo(response, endTime - startTime);

        } catch (Exception e) {
            System.err.println("❌ 请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行POST请求测试
     */
    private static void testPostRequest(String apiUrl, String requestBody) {
        try {
            System.out.println("\n--- 测试POST请求: " + apiUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "Bilibili-API-Test/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();

            printResponseInfo(response, endTime - startTime);

        } catch (Exception e) {
            System.err.println("❌ POST请求失败: " + e.getMessage());
        }
    }

    /**
     * 打印响应信息
     */
    private static void printResponseInfo(HttpResponse<String> response, long responseTime) {
        // 状态码颜色标识
        String statusColor = response.statusCode() == 200 ? "✅" :
                response.statusCode() == 404 ? "⚠️" : "❌";

        System.out.println(statusColor + " 状态码: " + response.statusCode());
        System.out.println("⏱️  响应时间: " + responseTime + "ms");
        System.out.println("📏 内容长度: " + response.body().length() + " 字符");

        // 显示部分响应头
        System.out.println("📋 响应头:");
        response.headers().map().forEach((key, values) -> {
            if (key.startsWith("content-type") || key.startsWith("date")) {
                System.out.println("   " + key + ": " + String.join(", ", values));
            }
        });

        // 格式化并显示响应体（限制长度避免控制台输出过多）
        System.out.println("📄 响应体:");
        String formattedBody = formatJson(response.body());
        if (formattedBody.length() > 1000) {
            System.out.println(formattedBody.substring(0, 1000) + "\n... (内容过长，已截断)");
        } else {
            System.out.println(formattedBody);
        }

        // 简单的结果判断
        if (response.statusCode() == 200) {
            System.out.println("🎉 请求成功");
        } else if (response.statusCode() == 404) {
            System.out.println("🔍 资源未找到");
        } else if (response.statusCode() >= 500) {
            System.out.println("💥 服务器错误");
        } else if (response.statusCode() == 400) {
            System.out.println("📝 请求参数错误");
        }
    }

    /**
     * 简单的JSON格式化方法
     */
    private static String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        try {
            // 简单的缩进格式化
            int indentLevel = 0;
            StringBuilder formatted = new StringBuilder();
            boolean inQuotes = false;

            for (char c : json.toCharArray()) {
                if (c == '\"') {
                    inQuotes = !inQuotes;
                    formatted.append(c);
                } else if (!inQuotes) {
                    if (c == '{' || c == '[') {
                        formatted.append(c).append("\n");
                        indentLevel++;
                        formatted.append("  ".repeat(indentLevel));
                    } else if (c == '}' || c == ']') {
                        formatted.append("\n");
                        indentLevel--;
                        formatted.append("  ".repeat(indentLevel));
                        formatted.append(c);
                    } else if (c == ',') {
                        formatted.append(c).append("\n");
                        formatted.append("  ".repeat(indentLevel));
                    } else if (c == ':') {
                        formatted.append(c).append(" ");
                    } else {
                        formatted.append(c);
                    }
                } else {
                    formatted.append(c);
                }
            }
            return formatted.toString();
        } catch (Exception e) {
            return json; // 如果格式化失败，返回原始JSON
        }
    }
}