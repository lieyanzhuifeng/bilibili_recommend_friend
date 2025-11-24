package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

}