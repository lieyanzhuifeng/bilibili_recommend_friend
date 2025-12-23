package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.CategoryRecommendationDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserTopCategory;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserTopCategoryRepository;
import com.bilibili.rec_system.repository.VideoCategoryRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryRecommendationService implements RecommendationService {

    @Autowired
    private UserTopCategoryRepository userTopCategoryRepository;  // 用户分区偏好

    @Autowired
    private VideoCategoryRepository videoCategoryRepository;      // 分区字典

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        return recommendUsers(userId, 5);
    }

    public List<BaseDTO> recommendUsers(Long userId, Integer n) {
        // 1. 获取目标用户的分区偏好
        List<UserTopCategory> targetCategories = userTopCategoryRepository.findTop3ByUserId(userId);
        if (targetCategories.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有其他用户
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // 3. 计算每个候选用户的分区匹配分数
        List<CategoryRecommendationDTO> result = new ArrayList<>();

        for (Long candidateUserId : allUserIds) {
            // 获取候选用户的分区偏好
            List<UserTopCategory> candidateCategories = userTopCategoryRepository.findTop3ByUserId(candidateUserId);

            if (candidateCategories.isEmpty()) {
                continue;
            }

            // 计算分区匹配分数
            CategoryMatchResult matchResult = calculateCategoryMatch(targetCategories, candidateCategories);

            if (matchResult.getScore() > 0) { // 只返回有共同分区的用户
                User candidateUser = userRepository.findByUserId(candidateUserId);
                if (candidateUser != null) {
                    CategoryRecommendationDTO dto = new CategoryRecommendationDTO(
                            candidateUser.getUserId(),
                            candidateUser.getUsername(),
                            candidateUser.getAvatarPath(),
                            matchResult.getScore(),
                            matchResult.getCommonCategoryNames()
                    );
                    result.add(dto);
                }
            }
        }

        // 4. 按分数排序并限制数量
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getCategoryMatchScore(), a.getCategoryMatchScore()))
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个用户的分区匹配分数（使用您的牛逼算法）
     */
    private CategoryMatchResult calculateCategoryMatch(List<UserTopCategory> user1Cats,
                                                       List<UserTopCategory> user2Cats) {
        double totalScore = 0.0;
        List<String> commonCategoryNames = new ArrayList<>();

        // 遍历用户1的每个分区，在用户2中找匹配
        for (UserTopCategory cat1 : user1Cats) {
            for (UserTopCategory cat2 : user2Cats) {
                if (cat1.getCategoryId().equals(cat2.getCategoryId())) {
                    // 计算权重差异
                    double prop1 = cat1.getProportion().doubleValue();
                    double prop2 = cat2.getProportion().doubleValue();
                    double weightDiff = Math.abs(prop1 - prop2);
                    // 您的牛逼算法：1 - 权重差异
                    double similarityContribution = 1 - weightDiff;
                    totalScore += similarityContribution;

                    // 获取分区名称
                    String categoryName = getCategoryName(cat1.getCategoryId());
                    commonCategoryNames.add(categoryName);
                    break;
                }
            }
        }

        return new CategoryMatchResult(totalScore, commonCategoryNames);
    }

    /**
     * 获取分区名称（从分区字典表获取）
     */
    private String getCategoryName(Long categoryId) {
        try {
            return videoCategoryRepository.findCategoryNameById(categoryId);
        } catch (Exception e) {
            return "未知分区";
        }
    }

    /**
     * 内部类：封装匹配结果
     */
    private static class CategoryMatchResult {
        private final double score;
        private final List<String> commonCategoryNames;

        public CategoryMatchResult(double score, List<String> commonCategoryNames) {
            this.score = score;
            this.commonCategoryNames = commonCategoryNames;
        }

        public double getScore() { return score; }
        public List<String> getCommonCategoryNames() { return commonCategoryNames; }
    }
    //子系统show接口的具体实现
    //提供用户画像的组件
    public Map<String, Double> show(Long userId) {
        // 获取用户的分区偏好
        List<UserTopCategory> userCategories = userTopCategoryRepository.findTop3ByUserId(userId);

        if (userCategories.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> distribution = new LinkedHashMap<>();

        for (UserTopCategory category : userCategories) {
            String categoryName = getCategoryName(category.getCategoryId());
            // 将BigDecimal转换为Double百分比（假设proportion是0-1的小数）
            Double percentage = category.getProportion().doubleValue() * 100;
            distribution.put(categoryName, percentage);
        }

        return distribution;
    }

}