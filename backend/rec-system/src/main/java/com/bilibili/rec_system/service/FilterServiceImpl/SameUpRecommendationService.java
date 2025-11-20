package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameUpRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.SameUpFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserUpStatsRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SameUpRecommendationService implements FilterService {

    @Autowired
    private UserUpStatsRepository userUpStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        SameUpFilterDTO sameUpFilter = (SameUpFilterDTO) filter;
        Long upId = sameUpFilter.getUpId();
        Integer durationOption = sameUpFilter.getDurationOption();

        // 获取UP主信息
        User upUser = userRepository.findByUserId(upId);
        if (upUser == null) return new ArrayList<>();
        String upName = upUser.getUsername();

        // 获取观看该UP主的用户
        List<UserUpStats> upStatsList = userUpStatsRepository.findByUpId(upId);

        List<SameUpRecommendationDTO> result = new ArrayList<>();

        for (UserUpStats upStats : upStatsList) {
            // 检查时长条件
            if (!matchesDurationOption(upStats.getWatchHours(), durationOption)) {
                continue;
            }

            // 获取用户信息
            User user = userRepository.findByUserId(upStats.getUserId());
            if (user == null) {
                continue;
            }

            // 创建DTO
            result.add(new SameUpRecommendationDTO(
                    user.getUserId(),
                    user.getUsername(),
                    user.getAvatarPath(),
                    upName,
                    upStats.getWatchHours()
            ));
        }

        // 排序
        result.sort((a, b) -> Double.compare(b.getWatchHours(), a.getWatchHours()));

        return new ArrayList<>(result);
    }

    private boolean matchesDurationOption(Double watchHours, Integer durationOption) {
        if (durationOption == null || durationOption == -1) {
            return true;
        }
        if (watchHours == null) return false;

        switch (durationOption) {
            case 0: return watchHours <= 1;      // 0-1小时
            case 1: return watchHours > 1 && watchHours <= 3;   // 1-3小时
            case 2: return watchHours > 3 && watchHours <= 10;  // 3-10小时
            case 3: return watchHours > 10 && watchHours <= 30; // 10-30小时
            case 4: return watchHours > 30;      // 30小时以上
            default: return true;
        }
    }
}