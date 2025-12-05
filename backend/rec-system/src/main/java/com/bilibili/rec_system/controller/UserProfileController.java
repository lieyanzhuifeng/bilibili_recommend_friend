package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 获取用户完整画像
     */
    @GetMapping("/show/{userId}")
    public Map<String, Object> showUserProfile(@PathVariable Long userId) {
        return userProfileService.show(userId);
    }

}