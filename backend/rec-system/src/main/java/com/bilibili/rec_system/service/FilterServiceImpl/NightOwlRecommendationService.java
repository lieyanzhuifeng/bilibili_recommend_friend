package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.NightOwlRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.NightOwlFilterDTO;
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
public class NightOwlRecommendationService implements FilterService {

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof NightOwlFilterDTO)) {
            throw new IllegalArgumentException("过滤器类型不正确");
        }

        NightOwlFilterDTO nightOwlFilter = (NightOwlFilterDTO) filter;
        String option = nightOwlFilter.getOption();

        return recommendUsers(option);
    }

    /**
     * 推荐指定等级的夜猫子用户
     */
    public List<BaseDTO> recommendUsers(String owlLevel) {
        List<NightOwlRecommendationDTO> result = new ArrayList<>();

        // 获取所有用户统计信息
        List<UserStatistics> allStats = userStatisticsRepository.findAll();

        for (UserStatistics stats : allStats) {
            // 计算夜猫子等级
            String userOwlLevel = calculateOwlLevel(stats);

            // 如果匹配筛选条件，则加入结果
            if (userOwlLevel.equals(owlLevel)) {
                User user = userRepository.findByUserId(stats.getUserId());
                if (user != null) {
                    // 分钟转小时
                    int watchHours = stats.getNightWatchMinutes() / 60;

                    NightOwlRecommendationDTO dto = new NightOwlRecommendationDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getAvatarPath(),
                            watchHours,
                            stats.getNightWatchDays()
                    );
                    result.add(dto);
                }
            }
        }

        // 按观看时长降序排序
        return result.stream()
                .sorted((a, b) -> Integer.compare(b.getWatchHours(), a.getWatchHours()))
                .collect(Collectors.toList());
    }

    /**
     * 计算夜猫子等级
     */
    public String calculateOwlLevel(UserStatistics stats) {
        int watchHours = stats.getNightWatchMinutes() / 60;
        int activeDays = stats.getNightWatchDays();

        // 分别计算两个维度的等级
        String watchLevel = getWatchLevel(watchHours);
        String activeLevel = getActiveLevel(activeDays);

        // 取较轻的等级
        return getHeavierLevel(watchLevel, activeLevel);
    }

    private String getWatchLevel(int watchHours) {
        if (watchHours <= 100) return "non_owl";
        else if (watchHours <= 300) return "light_owl";
        else if (watchHours <= 600) return "medium_owl";
        else return "heavy_owl";
    }

    private String getActiveLevel(int activeDays) {
        if (activeDays <= 100) return "non_owl";
        else if (activeDays <= 200) return "light_owl";
        else if (activeDays <= 300) return "medium_owl";
        else return "heavy_owl";
    }

    private String getHeavierLevel(String level1, String level2) {
        // 等级权重：non_owl < light_owl < medium_owl < heavy_owl
        String[] levels = {"non_owl", "light_owl", "medium_owl", "heavy_owl"};
        int index1 = Arrays.asList(levels).indexOf(level1);
        int index2 = Arrays.asList(levels).indexOf(level2);

        return levels[Math.max(index1, index2)];
    }
}