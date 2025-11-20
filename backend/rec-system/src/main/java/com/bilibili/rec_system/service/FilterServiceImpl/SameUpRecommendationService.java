package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameUpRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.SameUpFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.UserUpStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SameUpRecommendationService implements FilterService {

    @Autowired
    private UserUpStatsService userUpStatsService;  // 使用 Service 而不是 Repository

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof SameUpFilterDTO)) {
            throw new IllegalArgumentException("筛选条件类型不匹配，期望: SameUpFilterDTO");
        }

        SameUpFilterDTO sameUpFilter = (SameUpFilterDTO) filter;
        Long upId = sameUpFilter.getUpId();
        Integer durationOption = sameUpFilter.getDurationOption();

        // 使用 UserUpStatsService 获取数据（已经包含 Duration 转换）
        List<UserUpStats> upStatsList = userUpStatsService.findByUpId(upId);

        if (upStatsList.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据时长选项筛选并转换为DTO
        return upStatsList.stream()
                .filter(upStats -> matchesDurationOption(upStats.getTotalWatchDuration(), durationOption))
                .map(upStats -> {
                    User user = userRepository.findByUserId(upStats.getUserId());
                    if (user != null) {
                        return new SameUpRecommendationDTO(
                                user.getUserId(),
                                user.getUsername(),
                                user.getAvatarPath(),
                                "UP主" + upId,
                                upStats.getTotalWatchDuration() // 直接使用已经转换好的 Duration
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 检查观看时长是否符合筛选条件
     */
    private boolean matchesDurationOption(Duration duration, Integer durationOption) {
        if (durationOption == null || durationOption == -1) {
            return true; // 返回所有
        }

        long totalMinutes = duration.toMinutes();

        switch (durationOption) {
            case 0: return totalMinutes <= 10;     // 0-10分钟
            case 1: return totalMinutes > 10 && totalMinutes <= 50;  // 10-50分钟
            case 2: return totalMinutes > 50 && totalMinutes <= 100; // 50-100分钟
            case 3: return totalMinutes > 100 && totalMinutes <= 200; // 100-200分钟
            case 4: return totalMinutes > 200;     // 200分钟以上
            default: return true;
        }
    }
}