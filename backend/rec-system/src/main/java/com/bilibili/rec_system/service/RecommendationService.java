package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.User;

import java.util.List;

public interface RecommendationService {
    List<User> recommendUsers(Long userId);
}
