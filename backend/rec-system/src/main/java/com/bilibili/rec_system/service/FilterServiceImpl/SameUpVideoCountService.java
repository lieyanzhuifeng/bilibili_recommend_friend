package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameUpVideoCountDTO;
import com.bilibili.rec_system.dto.filter.SameUpVideoCountFilterDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserUpStats;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserUpStatsRepository;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SameUpVideoCountService implements FilterService {

    @Autowired
    private UserUpStatsRepository userUpStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    // 比例选项定义
    private static final Map<Integer, String> RATIO_LEVELS = Map.of(
            0, "0-20%",
            1, "20-40%",
            2, "40-60%",
            3, "60-80%",
            4, "80-100%"
    );

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof SameUpVideoCountFilterDTO)) {
            throw new IllegalArgumentException("筛选条件类型不匹配，期望: SameUpVideoCountFilterDTO");
        }

        SameUpVideoCountFilterDTO sameUpFilter = (SameUpVideoCountFilterDTO) filter;
        Long upId = sameUpFilter.getUpId();
        Integer ratioOption = sameUpFilter.getRatioOption();

        // 获取UP主名称
        User upUser = userRepository.findByUserId(upId);
        String upName = upUser != null ? upUser.getUsername() : "未知UP主";

        // 获取该UP主的总视频数量
        Long totalVideoCount = videoRepository.countVideosByUpId(upId);
        if (totalVideoCount == 0) {
            return new ArrayList<>();
        }

        // 获取所有观看过该UP主的用户
        List<UserUpStats> upStatsList = userUpStatsRepository.findByUpId(upId);

        // 根据观看比例筛选用户
        return upStatsList.stream()
                .map(upStats -> {
                    // 计算观看比例
                    double watchRatio = (double) upStats.getUniqueVideos() / totalVideoCount;

                    // 根据比例选项筛选
                    if (isInRatioRange(watchRatio, ratioOption)) {
                        User user = userRepository.findByUserId(upStats.getUserId());
                        if (user != null) {
                            return new SameUpVideoCountDTO(
                                    user.getUserId(),
                                    user.getUsername(),
                                    user.getAvatarPath(),
                                    upName,  // 使用真实的UP主名称
                                    upStats.getUniqueVideos(),
                                    totalVideoCount.intValue(),
                                    watchRatio
                            );
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getWatchRatio(), a.getWatchRatio()))
                .collect(Collectors.toList());
    }

    /**
     * 判断观看比例是否在指定范围内
     */
    private boolean isInRatioRange(double ratio, Integer option) {
        switch (option) {
            case 0: return ratio >= 0.0 && ratio < 0.2;
            case 1: return ratio >= 0.2 && ratio < 0.4;
            case 2: return ratio >= 0.4 && ratio < 0.6;
            case 3: return ratio >= 0.6 && ratio < 0.8;
            case 4: return ratio >= 0.8 && ratio <= 1.0;
            default: return true;
        }
    }

    /**
     * 获取比例选项列表
     */
    public Map<Integer, String> getRatioLevels() {
        return RATIO_LEVELS;
    }
}