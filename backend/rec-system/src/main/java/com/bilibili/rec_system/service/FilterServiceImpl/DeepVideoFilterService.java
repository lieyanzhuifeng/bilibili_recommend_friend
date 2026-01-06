package com.bilibili.rec_system.service.FilterServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.DeepVideoRecommendationDTO;
import com.bilibili.rec_system.dto.filter.DeepVideoFilterDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.UserVideoStats;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.UserVideoStatsRepository;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeepVideoFilterService implements FilterService {

    @Autowired
    private UserVideoStatsRepository userVideoStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public List<BaseDTO> filterUsers(FilterBaseDTO filter) {
        if (!(filter instanceof DeepVideoFilterDTO deepVideoFilter)) {
            throw new IllegalArgumentException("过滤器类型不匹配");
        }

        Long videoId = deepVideoFilter.getVideoId();
        Integer option = deepVideoFilter.getOption();

        return filterUsersByOption(videoId, option);
    }

    /**
     * 根据option选项筛选用户
     */
    public List<BaseDTO> filterUsersByOption(Long videoId, Integer option) {
        // 获取视频时长（秒数）
        LocalTime videoDurationTime = videoRepository.findDurationByVideoId(videoId);
        if (videoDurationTime == null) {
            return new ArrayList<>();
        }

        // 将 LocalTime 转换为秒数
        Double videoDurationSeconds = (double) convertToSeconds(videoDurationTime);
        List<UserVideoStats> statsList;

        switch (option) {
            case 0: // 深度观看：观看次数≥5 或 观看时长≥2倍视频时长
                Double minDuration0 = videoDurationSeconds * 2;
                statsList = userVideoStatsRepository.findByVideoIdAndMinCountOrDuration(
                        videoId, 5, secondsToLocalTime(minDuration0.longValue()));
                break;
            case 1: // 极其深度观看：观看次数≥10 或 观看时长≥5倍视频时长
                Double minDuration1 = videoDurationSeconds * 5;
                statsList = userVideoStatsRepository.findByVideoIdAndMinCountOrDuration(
                        videoId, 10, secondsToLocalTime(minDuration1.longValue()));
                break;
            default:
                statsList = new ArrayList<>();
        }

        return convertToDTO(statsList, videoId);
    }

    /**
     * 获取指定用户的深度观看视频列表（使用option 0的标准）
     *
     * @param userId 用户ID
     * @return 深度观看视频的DTO列表
     */
    public List<DeepVideoRecommendationDTO> show(Long userId) {
        // 深度观看标准：观看次数≥5 或 观看时长≥2倍视频时长
        final int MIN_WATCH_COUNT = 5;
        final double MIN_DURATION_MULTIPLIER = 2.0;

        // 1. 获取用户所有的观看统计
        List<UserVideoStats> allStats = userVideoStatsRepository.findByUserId(userId);
        if (allStats.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeepVideoRecommendationDTO> deepVideos = new ArrayList<>();

        for (UserVideoStats stats : allStats) {
            Long videoId = stats.getVideoId();

            // 获取视频时长
            LocalTime videoDurationTime = videoRepository.findDurationByVideoId(videoId);
            if (videoDurationTime == null) {
                continue;
            }

            // 计算最小时长要求（秒数）
            Double videoDurationSeconds = (double) convertToSeconds(videoDurationTime);
            Double minDurationSeconds = videoDurationSeconds * MIN_DURATION_MULTIPLIER;

            // 判断是否满足深度观看条件
            boolean isDeepWatch = stats.getWatchCount() >= MIN_WATCH_COUNT ||
                    (stats.getTotalWatchDuration() != null &&
                            stats.getTotalWatchDuration() >= minDurationSeconds);

            if (isDeepWatch) {
                // 获取视频标题
                String videoTitle = getVideoTitle(videoId);
                // 获取用户信息
                User user = userRepository.findByUserId(userId);
                if (user != null) {
                    DeepVideoRecommendationDTO dto = new DeepVideoRecommendationDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getAvatarPath(),
                            videoId,
                            videoTitle,
                            stats.getTotalWatchDuration(),
                            stats.getWatchCount()
                    );
                    deepVideos.add(dto);
                }
            }
        }

        return deepVideos;
    }

    /**
     * 转换为DTO
     */
    private List<BaseDTO> convertToDTO(List<UserVideoStats> statsList, Long videoId) {
        List<BaseDTO> result = new ArrayList<>();

        if (statsList.isEmpty()) {
            return result;
        }

        // 获取视频标题
        String videoTitle = getVideoTitle(videoId);

        for (UserVideoStats stats : statsList) {
            User user = userRepository.findByUserId(stats.getUserId());
            if (user != null) {
                DeepVideoRecommendationDTO dto = new DeepVideoRecommendationDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatarPath(),
                        videoId,
                        videoTitle,
                        stats.getTotalWatchDuration(),
                        stats.getWatchCount()
                );
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 获取视频标题
     */
    private String getVideoTitle(Long videoId) {
        try {
            List<String> titles = videoRepository.findTitlesByVideoIdIn(List.of(videoId));
            return titles.isEmpty() ? "未知视频" : titles.get(0);
        } catch (Exception e) {
            return "未知视频";
        }
    }

    /**
     * 秒数（Long）转换为LocalTime
     */
    private LocalTime secondsToLocalTime(long totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int secs = (int) (totalSeconds % 60);
        return LocalTime.of(hours, minutes, secs);
    }

    /**
     * LocalTime转换为秒数
     */
    private long convertToSeconds(LocalTime time) {
        if (time == null) return 0;
        return time.getHour() * 3600L + time.getMinute() * 60L + time.getSecond();
    }
}