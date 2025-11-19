// RecommendationService.java
package com.bilibili.rec_system.service;

import com.bilibili.rec_system.dto.BaseDTO;
import java.util.List;

public interface RecommendationService {
    List<BaseDTO> recommendUsers(Long userId);
}