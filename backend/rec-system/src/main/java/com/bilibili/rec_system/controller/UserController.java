package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.Video;
import com.bilibili.rec_system.entity.Tag;
import com.bilibili.rec_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 通过用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                response.put("success", true);
                response.put("data", user);
                response.put("message", "获取用户信息成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据用户名模糊搜索用户
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsersByUsername(
            @RequestParam String keyword) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 验证参数
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索关键词不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 去除前后空格
            String searchKeyword = keyword.trim();

            // 执行搜索
            List<User> users = userService.searchUsersByUsername(searchKeyword);

            response.put("success", true);
            response.put("data", users);
            response.put("total", users.size());
            response.put("keyword", searchKeyword);
            response.put("message", "搜索完成，找到 " + users.size() + " 个用户");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据视频标题模糊搜索视频
     * @param keyword 搜索关键词
     * @return 匹配的视频列表
     */
    @GetMapping("/videos/search")
    public ResponseEntity<Map<String, Object>> searchVideosByTitle(
            @RequestParam String keyword) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 验证参数
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索关键词不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 去除前后空格
            String searchKeyword = keyword.trim();

            // 执行搜索
            List<Video> videos = userService.searchVideosByTitle(searchKeyword);

            response.put("success", true);
            response.put("data", videos);
            response.put("total", videos.size());
            response.put("keyword", searchKeyword);
            response.put("message", "搜索完成，找到 " + videos.size() + " 个视频");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据标签名称模糊搜索标签
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    @GetMapping("/tags/search")
    public ResponseEntity<Map<String, Object>> searchTagsByName(
            @RequestParam String keyword) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 验证参数
            if (keyword == null || keyword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "搜索关键词不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 去除前后空格
            String searchKeyword = keyword.trim();

            // 执行搜索
            List<Tag> tags = userService.searchTagsByName(searchKeyword);

            response.put("success", true);
            response.put("data", tags);
            response.put("total", tags.size());
            response.put("keyword", searchKeyword);
            response.put("message", "搜索完成，找到 " + tags.size() + " 个标签");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}