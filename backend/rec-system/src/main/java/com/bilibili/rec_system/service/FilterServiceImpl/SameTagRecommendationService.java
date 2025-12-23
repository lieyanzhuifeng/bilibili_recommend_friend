package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameTagRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.SameTagFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserTagStats;
import com.bilibili.rec_system.repository.TagRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserTagStatsRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

//策略模式的具体实现
@Service
public class SameTagRecommendationService implements FilterService {

    @Autowired
    private UserTagStatsRepository userTagStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;  // 新增

    private static final Map<Integer, String> DURATION_LEVELS = Map.of(
            0, "0-1小时",
            1, "1-3小时",
            2, "3-10小时",
            3, "10-30小时",
            4, "30小时以上"
    );

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof SameTagFilterDTO sameTagFilter)) {
            throw new IllegalArgumentException("筛选条件类型不匹配，期望: SameTagFilterDTO");
        }

        Long tagId = sameTagFilter.getTagId();
        Integer durationOption = sameTagFilter.getDurationOption();

        List<UserTagStats> tagStatsList = getUsersByDurationLevel(tagId, durationOption);

        if (tagStatsList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取标签名称
        String tagName = getTagName(tagId);

        return tagStatsList.stream()
                .map(tagStats -> {
                    User user = userRepository.findByUserId(tagStats.getUserId());
                    if (user != null) {
                        return new SameTagRecommendationDTO(
                                user.getUserId(),
                                user.getUsername(),
                                user.getAvatarPath(),
                                tagName,  // 使用真实的标签名称
                                tagStats.getWatchHours()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getTotalWatchHours(), a.getTotalWatchHours()))
                .collect(Collectors.toList());
    }

    /**
     * 获取标签名称
     */
    private String getTagName(Long tagId) {
        try {
            String tagName = tagRepository.findTagNameByTagId(tagId);
            return tagName != null ? tagName : "未知标签";
        } catch (Exception e) {
            return "标签" + tagId;
        }
    }

    private List<UserTagStats> getUsersByDurationLevel(Long tagId, Integer level) {
        return switch (level) {
            case 0 -> userTagStatsRepository.findByTagIdAndHoursRange(tagId, 0.0, 1.0);
            case 1 -> userTagStatsRepository.findByTagIdAndHoursRange(tagId, 1.0, 3.0);
            case 2 -> userTagStatsRepository.findByTagIdAndHoursRange(tagId, 3.0, 10.0);
            case 3 -> userTagStatsRepository.findByTagIdAndHoursRange(tagId, 10.0, 30.0);
            case 4 -> userTagStatsRepository.findByTagIdAndMinHours(tagId, 30.0);
            default -> userTagStatsRepository.findByTagId(tagId);
        };
    }

    public Map<Integer, String> getDurationLevels() {
        return DURATION_LEVELS;
    }
}