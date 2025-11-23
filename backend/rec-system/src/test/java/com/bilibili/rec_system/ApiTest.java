package com.bilibili.rec_system;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiTest {
    public static void main(String[] args) {
        // API地址 - 根据你的实际服务地址修改
        String apiUrl = "http://localhost:8080/api/recommend/comment-friends/1";

        try {
            // 创建HttpClient
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 创建HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // 打印响应信息
            System.out.println("状态码: " + response.statusCode());
            System.out.println("响应头: " + response.headers().map());
            System.out.println("响应体: ");
            System.out.println(formatJson(response.body()));

        } catch (Exception e) {
            System.err.println("调用API失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 简单的JSON格式化方法
    private static String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        try {
            // 简单的缩进格式化
            int indentLevel = 0;
            StringBuilder formatted = new StringBuilder();

            for (char c : json.toCharArray()) {
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
                } else {
                    formatted.append(c);
                }
            }
            return formatted.toString();
        } catch (Exception e) {
            // 如果格式化失败，返回原始JSON
            return json;
        }
    }
}