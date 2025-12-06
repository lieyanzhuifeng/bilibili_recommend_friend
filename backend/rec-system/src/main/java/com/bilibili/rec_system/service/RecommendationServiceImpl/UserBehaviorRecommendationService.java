package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.dto.UserBehaviorRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserStatistics;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserStatisticsRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBehaviorRecommendationService implements RecommendationService {

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        List<UserBehaviorRecommendationDTO> result = new ArrayList<>();

        // 获取当前用户的统计信息
        UserStatistics currentUserStats = userStatisticsRepository.findByUserId(userId);
        if (currentUserStats == null) {
            return new ArrayList<>(); // 用户没有统计信息
        }

        // 获取所有其他用户的统计信息
        List<UserStatistics> allOtherStats = userStatisticsRepository.findAllExcludingUser(userId);

        for (UserStatistics otherStats : allOtherStats) {
            // 计算余弦相似度
            double similarityScore = calculateCosineSimilarity(currentUserStats, otherStats);

            // 只推荐相似度较高的用户
            if (similarityScore > 0.7) {
                // 获取用户信息并创建DTO
                User otherUser = userRepository.findByUserId(otherStats.getUserId());
                if (otherUser != null) {
                    UserBehaviorRecommendationDTO dto = new UserBehaviorRecommendationDTO(
                            otherUser.getUserId(),
                            otherUser.getUsername(),
                            otherUser.getAvatarPath(),
                            similarityScore
                    );
                    result.add(dto);
                }
            }
        }

        // 按相似度降序排序，限制返回前5个
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(UserStatistics stats1, UserStatistics stats2) {
        // 提取四个行为指标作为向量
        double[] vector1 = {
                stats1.getLikeRate().doubleValue(),
                stats1.getCoinRate().doubleValue(),
                stats1.getFavoriteRate().doubleValue(),
                stats1.getShareRate().doubleValue()
        };

        double[] vector2 = {
                stats2.getLikeRate().doubleValue(),
                stats2.getCoinRate().doubleValue(),
                stats2.getFavoriteRate().doubleValue(),
                stats2.getShareRate().doubleValue()
        };

        // 计算点积
        double dotProduct = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
        }

        // 计算向量模长
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    public Map<String, Object> show(Long userId) {
        UserStatistics userStats = userStatisticsRepository.findByUserId(userId);

        if (userStats == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> behaviorStats = new LinkedHashMap<>();

        // 根据实际表结构设置字段
        behaviorStats.put("totalWatchHours", userStats.getTotalWatchHours());
        behaviorStats.put("likeRate", userStats.getLikeRate().doubleValue() * 100); // 转百分比
        behaviorStats.put("coinRate", userStats.getCoinRate().doubleValue() * 100);
        behaviorStats.put("favoriteRate", userStats.getFavoriteRate().doubleValue() * 100);
        behaviorStats.put("shareRate", userStats.getShareRate().doubleValue() * 100);
        behaviorStats.put("activeDays", userStats.getActiveDays());
        behaviorStats.put("nightWatchMinutes", userStats.getNightWatchMinutes());
        behaviorStats.put("nightWatchDays", userStats.getNightWatchDays());

        return behaviorStats;
    }

}