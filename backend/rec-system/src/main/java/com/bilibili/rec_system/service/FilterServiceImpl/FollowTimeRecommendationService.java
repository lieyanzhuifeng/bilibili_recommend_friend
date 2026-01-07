package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FollowTimeRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.FollowTimeFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.UserFollowUpRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowTimeRecommendationService implements FilterService {

    @Autowired
    private UserFollowUpRepository userFollowUpRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof FollowTimeFilterDTO)) {
            throw new IllegalArgumentException("过滤器类型不正确");
        }

        FollowTimeFilterDTO followTimeFilter = (FollowTimeFilterDTO) filter;
        Long userId = followTimeFilter.getUserId();
        Long upId = followTimeFilter.getUpId();

        return recommendUsers(userId, upId);
    }

    /**
     * 推荐关注时间有缘分的用户
     */
    public List<BaseDTO> recommendUsers(Long userId, Long upId) {
        List<FollowTimeRecommendationDTO> result = new ArrayList<>();

        // 获取当前用户的关注天数
        Integer userFollowDays = userFollowUpRepository.findFollowDaysByUserAndUP(userId, upId);
        if (userFollowDays == null) {
            return new ArrayList<>(); // 用户没有关注该UP主
        }

        // 计算当前用户的关注时长得分（维度1）
        int userFollowDurationScore = calculateFollowDurationScore(userFollowDays);

        // 查找所有关注了该UP主的其他用户
        List<Long> allOtherUserIds = userFollowUpRepository.findUsersByUpIdExcludingUser(upId, userId);

        // 获取UP主名称
        String upName = getUpName(upId);

        for (Long otherUserId : allOtherUserIds) {
            // 获取其他用户的关注天数
            Integer otherUserFollowDays = userFollowUpRepository.findFollowDaysByUserAndUP(otherUserId, upId);
            if (otherUserFollowDays == null) continue;

            // 计算时间差（绝对值）
            int timeDifference = Math.abs(userFollowDays - otherUserFollowDays);

            // 计算时间差得分（维度2）
            int timeDiffScore = calculateTimeDifferenceScore(timeDifference);

            // 计算总推荐分数（如果时间差得分是0，总分为0）
            int totalScore = (timeDiffScore == 0) ? 0 : userFollowDurationScore + timeDiffScore;

            // 只有总分>0的用户才加入结果
            if (totalScore > 0) {
                // 生成推荐理由
                String recommendationReason = generateRecommendationReason(
                        userFollowDays, timeDifference, upName
                );

                // 获取用户信息并创建DTO
                User otherUser = userRepository.findByUserId(otherUserId);
                if (otherUser != null) {
                    FollowTimeRecommendationDTO dto = new FollowTimeRecommendationDTO(
                            otherUser.getUserId(),
                            otherUser.getUsername(),
                            otherUser.getAvatarPath(),
                            recommendationReason,
                            totalScore
                    );
                    result.add(dto);
                }
            }
        }

        // 按推荐分数降序排序，限制返回前3个
        return result.stream()
                .sorted((a, b) -> Integer.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 维度1：计算关注时长得分
     */
    private int calculateFollowDurationScore(int followDays) {
        int followMonths = followDays / 30; // 近似月数

        if (followMonths < 1) return 0;      // 0-1个月：0分
        else if (followMonths < 3) return 1; // 1-3个月：1分
        else if (followMonths < 6) return 2; // 3-6个月：2分
        else if (followMonths < 12) return 3; // 6-12个月：3分
        else if (followMonths < 36) return 4; // 1-3年：4分
        else return 5; // 3年以上：5分
    }

    /**
     * 维度2：计算时间差得分
     */
    private int calculateTimeDifferenceScore(int timeDifferenceDays) {
        if (timeDifferenceDays == 0) return 5;        // 同一天：5分
        else if (timeDifferenceDays <= 3) return 4;   // 1-3天：4分
        else if (timeDifferenceDays <= 7) return 3;   // 3-7天：3分
        else if (timeDifferenceDays <= 14) return 2;  // 1-2周：2分
        else if (timeDifferenceDays <= 30) return 1;  // 2周-1个月：1分
        else return 0; // 超过1个月：0分
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(int userFollowDays, int timeDifference, String upName) {
        return String.format("你们关注%s相差%d天，你关注了该up主%d天了",
                upName, timeDifference, userFollowDays);
    }

    /**
     * 获取UP主名称
     */
    private String getUpName(Long upId) {
        User upUser = userRepository.findByUserId(upId);
        if (upUser != null) {
            return upUser.getUsername();
        }
        return "UP主" + upId;
    }
}