package com.bilibili.rec_system;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UserApiTest {

    private static final String BASE_URL = "http://localhost:8080/api/users";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) {
        // 测试存在的用户ID
        testGetUserById(1L);

        // 测试不存在的用户ID
        testGetUserById(9999L);

        // 测试无效的用户ID
        testGetUserById(-1L);
    }

    public static void testGetUserById(Long userId) {
        try {
            String url = BASE_URL + "/" + userId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            System.out.println("=== 测试用户ID: " + userId + " ===");
            System.out.println("请求URL: " + url);

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            // 打印响应信息
            printResponseInfo(response);

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void printResponseInfo(HttpResponse<String> response) {
        System.out.println("响应状态码: " + response.statusCode());
        System.out.println("响应头: " + response.headers().map());

        String body = response.body();
        System.out.println("响应体 JSON:");
        System.out.println(formatJson(body));
    }

    /**
     * 简单的 JSON 格式化方法
     */
    private static String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "空响应";
        }

        try {
            // 简单的格式化：添加缩进和换行
            int indentLevel = 0;
            StringBuilder formatted = new StringBuilder();
            boolean inQuotes = false;

            for (char c : json.toCharArray()) {
                if (c == '"') {
                    inQuotes = !inQuotes;
                    formatted.append(c);
                } else if (!inQuotes) {
                    switch (c) {
                        case '{':
                        case '[':
                            formatted.append(c).append("\n");
                            indentLevel++;
                            formatted.append("  ".repeat(indentLevel));
                            break;
                        case '}':
                        case ']':
                            formatted.append("\n");
                            indentLevel--;
                            formatted.append("  ".repeat(indentLevel));
                            formatted.append(c);
                            break;
                        case ',':
                            formatted.append(c).append("\n");
                            formatted.append("  ".repeat(indentLevel));
                            break;
                        case ':':
                            formatted.append(c).append(" ");
                            break;
                        default:
                            formatted.append(c);
                    }
                } else {
                    formatted.append(c);
                }
            }
            return formatted.toString();
        } catch (Exception e) {
            // 如果格式化失败，返回原始 JSON
            return json;
        }
    }
}