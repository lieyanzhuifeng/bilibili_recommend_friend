package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SharedVideoRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserVideoStatsRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SharedVideoRecommendationService implements RecommendationService {

    @Autowired
    private UserVideoStatsRepository userVideoStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        return recommendUsers(userId, 5);
    }

    public List<BaseDTO> recommendUsers(Long userId, Integer n) {
        // 1. 获取目标用户的观看视频列表
        List<Long> targetVideoIds = userVideoStatsRepository.findWatchedVideoIdsByUser(userId);
        Set<Long> targetVideoSet = new HashSet<>(targetVideoIds);

        if (targetVideoSet.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有用户ID（排除目标用户）
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // 3. 计算每个候选用户的相似度并转换为DTO
        return allUserIds.stream()
                .map(candidateUserId -> {
                    // 获取候选用户的观看视频列表
                    List<Long> candidateVideoIds = userVideoStatsRepository.findWatchedVideoIdsByUser(candidateUserId);
                    Set<Long> candidateVideoSet = new HashSet<>(candidateVideoIds);

                    // 计算交集（共同看过的视频）
                    Set<Long> intersection = new HashSet<>(targetVideoSet);
                    intersection.retainAll(candidateVideoSet);

                    // 计算并集（两个用户看过的所有不同视频）
                    Set<Long> union = new HashSet<>(targetVideoSet);
                    union.addAll(candidateVideoSet);

                    // 计算相似度：交集数量 / 并集数量
                    double similarityRate = (double) intersection.size() / union.size();

                    // 只保留有一定相似度的用户
                    if (similarityRate > 0.1) {
                        User user = userRepository.findByUserId(candidateUserId);
                        if (user != null) {
                            return new SharedVideoRecommendationDTO(
                                    user.getUserId(),
                                    user.getUsername(),
                                    user.getAvatarPath(),
                                    similarityRate
                            );
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getSimilarityRate(), a.getSimilarityRate()))
                .limit(n)
                .collect(Collectors.toList());
    }
}