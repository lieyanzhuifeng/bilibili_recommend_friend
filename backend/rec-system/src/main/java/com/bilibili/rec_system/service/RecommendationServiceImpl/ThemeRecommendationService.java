package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.dto.ThemeRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserTopTheme;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserTopThemeRepository;
import com.bilibili.rec_system.repository.VideoThemeRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThemeRecommendationService implements RecommendationService {

    @Autowired
    private UserTopThemeRepository userTopThemeRepository;

    @Autowired
    private VideoThemeRepository videoThemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        return recommendUsers(userId, 5);
    }

    public List<BaseDTO> recommendUsers(Long userId, Integer n) {
        // 1. 获取目标用户的主题偏好
        List<UserTopTheme> targetThemes = userTopThemeRepository.findTop3ByUserId(userId);
        if (targetThemes.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有其他用户
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // 3. 计算每个候选用户的主题匹配分数
        List<ThemeRecommendationDTO> result = new ArrayList<>();

        for (Long candidateUserId : allUserIds) {
            // 获取候选用户的主题偏好
            List<UserTopTheme> candidateThemes = userTopThemeRepository.findTop3ByUserId(candidateUserId);

            if (candidateThemes.isEmpty()) {
                continue;
            }

            // 计算主题匹配分数
            ThemeMatchResult matchResult = calculateThemeMatch(targetThemes, candidateThemes);

            if (matchResult.getScore() > 0) { // 只返回有共同主题的用户
                User candidateUser = userRepository.findByUserId(candidateUserId);
                if (candidateUser != null) {
                    ThemeRecommendationDTO dto = new ThemeRecommendationDTO(
                            candidateUser.getUserId(),
                            candidateUser.getUsername(),
                            candidateUser.getAvatarPath(),
                            matchResult.getScore(),
                            matchResult.getCommonThemeNames()
                    );
                    result.add(dto);
                }
            }
        }

        // 4. 按分数排序并限制数量
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getThemeMatchScore(), a.getThemeMatchScore()))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个用户的主题匹配分数（使用您的牛逼算法）
     */
    private ThemeMatchResult calculateThemeMatch(List<UserTopTheme> user1Themes,
                                                 List<UserTopTheme> user2Themes) {
        double totalScore = 0.0;
        List<String> commonThemeNames = new ArrayList<>();

        // 遍历用户1的每个主题，在用户2中找匹配
        for (UserTopTheme theme1 : user1Themes) {
            for (UserTopTheme theme2 : user2Themes) {
                if (theme1.getThemeId().equals(theme2.getThemeId())) {
                    // 计算权重差异
                    double prop1 = theme1.getProportion().doubleValue();
                    double prop2 = theme2.getProportion().doubleValue();
                    double weightDiff = Math.abs(prop1 - prop2);
                    // 您的牛逼算法：1 - 权重差异
                    double similarityContribution = 1 - weightDiff;
                    totalScore += similarityContribution;

                    // 获取主题名称
                    String themeName = getThemeName(theme1.getThemeId());
                    commonThemeNames.add(themeName);
                    break;
                }
            }
        }

        return new ThemeMatchResult(totalScore, commonThemeNames);
    }

    /**
     * 获取主题名称（从主题字典表获取）
     */
    private String getThemeName(Long themeId) {
        try {
            return videoThemeRepository.findThemeNameById(themeId);
        } catch (Exception e) {
            return "未知主题";
        }
    }

    /**
     * 内部类：封装匹配结果
     */
    private static class ThemeMatchResult {
        private final double score;
        private final List<String> commonThemeNames;

        public ThemeMatchResult(double score, List<String> commonThemeNames) {
            this.score = score;
            this.commonThemeNames = commonThemeNames;
        }

        public double getScore() { return score; }
        public List<String> getCommonThemeNames() { return commonThemeNames; }
    }
    //不同的子系统的数据，都向外暴露一个show接口
    public Map<String, Double> show(Long userId) {
        // 获取用户的主题偏好
        List<UserTopTheme> userThemes = userTopThemeRepository.findTop3ByUserId(userId);

        if (userThemes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> distribution = new LinkedHashMap<>();

        for (UserTopTheme theme : userThemes) {
            String themeName = getThemeName(theme.getThemeId());
            // 将BigDecimal转换为Double百分比
            Double percentage = theme.getProportion().doubleValue() * 100;
            distribution.put(themeName, percentage);
        }

        return distribution;
    }

}