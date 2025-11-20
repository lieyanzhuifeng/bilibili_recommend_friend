package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameTagVideoCountDTO;
import com.bilibili.rec_system.dto.filter.SameTagVideoCountFilterDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserTagStats;
import com.bilibili.rec_system.repository.*;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SameTagVideoCountService implements FilterService {

    @Autowired
    private UserTagStatsRepository userTagStatsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TagVideoRepository tagVideoRepository; // 改为这个
    @Autowired
    private TagRepository tagRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        SameTagVideoCountFilterDTO tagFilter = (SameTagVideoCountFilterDTO) filter;
        Long tagId = tagFilter.getTagId();
        Integer ratioOption = tagFilter.getRatioOption();

        // 获取标签名称
        String tagName = tagRepository.findTagNameByTagId(tagId);

        // 从 TagVideoRepository 获取该标签的总视频数量
        Long totalVideoCount = tagVideoRepository.countVideosByTagId(tagId);
        if (totalVideoCount == 0) return new ArrayList<>();

        // 获取所有观看过该标签的用户
        List<UserTagStats> tagStatsList = userTagStatsRepository.findByTagId(tagId);

        // 根据观看比例筛选用户
        return tagStatsList.stream()
                .map(tagStats -> {
                    double watchRatio = (double) tagStats.getUniqueVideos() / totalVideoCount;

                    if (isInRatioRange(watchRatio, ratioOption)) {
                        User user = userRepository.findByUserId(tagStats.getUserId());
                        if (user != null) {
                            return new SameTagVideoCountDTO(
                                    user.getUserId(),
                                    user.getUsername(),
                                    user.getAvatarPath(),
                                    tagName,
                                    tagStats.getUniqueVideos(),
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
}