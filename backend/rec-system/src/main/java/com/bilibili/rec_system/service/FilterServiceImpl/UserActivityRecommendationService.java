package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.UserActivityRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.UserActivityFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserStatistics;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserStatisticsRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserActivityRecommendationService implements FilterService {

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof UserActivityFilterDTO)) {
            throw new IllegalArgumentException("过滤器类型不正确");
        }

        UserActivityFilterDTO activityFilter = (UserActivityFilterDTO) filter;
        String option = activityFilter.getOption();

        return recommendUsers(option);
    }

    /**
     * 推荐指定等级的用户活跃度
     */
    public List<BaseDTO> recommendUsers(String userLevel) {
        List<UserActivityRecommendationDTO> result = new ArrayList<>();

        // 获取所有用户统计信息
        List<UserStatistics> allStats = userStatisticsRepository.findAll();

        for (UserStatistics stats : allStats) {
            // 计算用户活跃等级
            String calculatedLevel = calculateUserLevel(stats);

            // 如果匹配筛选条件，则加入结果
            if (calculatedLevel.equals(userLevel)) {
                User user = userRepository.findByUserId(stats.getUserId());
                if (user != null) {
                    UserActivityRecommendationDTO dto = new UserActivityRecommendationDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getAvatarPath(),
                            stats.getTotalWatchHours().doubleValue(),
                            stats.getActiveDays()
                    );
                    result.add(dto);
                }
            }
        }

        // 按观看时长降序排序
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getTotalWatchHours(), a.getTotalWatchHours()))
                .collect(Collectors.toList());
    }

    /**
     * 计算用户活跃等级
     */
    public String calculateUserLevel(UserStatistics stats) {
        double watchHours = stats.getTotalWatchHours().doubleValue();
        int activeDays = stats.getActiveDays();

        // 分别计算两个维度的等级
        String watchLevel = getWatchLevel(watchHours);
        String activeLevel = getActiveLevel(activeDays);

        // 取较轻的等级
        return getHeavierLevel(watchLevel, activeLevel);
    }

    private String getWatchLevel(double watchHours) {
        if (watchHours <= 200) return "light_user";
        else if (watchHours <= 600) return "medium_user";
        else return "heavy_user";
    }

    private String getActiveLevel(int activeDays) {
        if (activeDays <= 150) return "light_user";
        else if (activeDays <= 300) return "medium_user";
        else return "heavy_user";
    }

    private String getHeavierLevel(String level1, String level2) {
        // 等级权重：light_user < medium_user < heavy_user
        String[] levels = {"light_user", "medium_user", "heavy_user"};
        int index1 = Arrays.asList(levels).indexOf(level1);
        int index2 = Arrays.asList(levels).indexOf(level2);

        return levels[Math.max(index1, index2)];
    }
}