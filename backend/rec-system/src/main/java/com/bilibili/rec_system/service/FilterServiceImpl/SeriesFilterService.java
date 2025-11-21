package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SeriesRecommendationDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.dto.filter.SeriesFilterDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserVideoStats;
import com.bilibili.rec_system.repository.TagRepository;
import com.bilibili.rec_system.repository.TagVideoRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserVideoStatsRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeriesFilterService implements FilterService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagVideoRepository tagVideoRepository;

    @Autowired
    private UserVideoStatsRepository userVideoStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof SeriesFilterDTO)) {
            throw new IllegalArgumentException("过滤器类型不匹配");
        }

        SeriesFilterDTO seriesFilter = (SeriesFilterDTO) filter;
        Long tagId = seriesFilter.getTagId();

        return filterUsersBySeries(tagId);
    }

    /**
     * 根据系列标签筛选看完整个系列的用户
     */
    public List<BaseDTO> filterUsersBySeries(Long tagId) {
        // 1. 验证是否为系列标签
        Boolean isSeries = tagRepository.isSeriesTag(tagId);
        if (isSeries == null || !isSeries) {
            return new ArrayList<>();
        }

        // 2. 获取系列名称
        String seriesName = tagRepository.findTagNameByTagId(tagId);
        if (seriesName == null) {
            return new ArrayList<>();
        }

        // 3. 获取该系列的所有视频ID
        List<Long> seriesVideoIds = tagVideoRepository.findVideoIdsByTag(tagId);
        if (seriesVideoIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 获取所有观看过该系列视频的用户记录
        List<UserVideoStats> allSeriesStats = userVideoStatsRepository.findByVideoIdIn(seriesVideoIds);

        // 5. 按用户分组，统计每个用户观看的系列视频数量
        Map<Long, Set<Long>> userWatchedSeriesVideos = new HashMap<>();

        for (UserVideoStats stats : allSeriesStats) {
            userWatchedSeriesVideos
                    .computeIfAbsent(stats.getUserId(), k -> new HashSet<>())
                    .add(stats.getVideoId());
        }

        // 6. 筛选看完整个系列的用户
        List<BaseDTO> result = new ArrayList<>();
        int totalSeriesVideos = seriesVideoIds.size();

        for (Map.Entry<Long, Set<Long>> entry : userWatchedSeriesVideos.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> watchedVideoIds = entry.getValue();

            // 检查用户是否看完了整个系列
            if (watchedVideoIds.size() == totalSeriesVideos) {
                User user = userRepository.findByUserId(userId);
                if (user != null) {
                    SeriesRecommendationDTO dto = new SeriesRecommendationDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getAvatarPath(),
                            seriesName
                    );
                    result.add(dto);
                }
            }
        }

        return result;
    }
}