// RecommendationService.java
package com.bilibili.rec_system.service;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.fasterxml.jackson.databind.ser.Serializers;

import java.util.List;

public interface RecommendationService {
    List<BaseDTO> recommendUsers(Long userId);
}