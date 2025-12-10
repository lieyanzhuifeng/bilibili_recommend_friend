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
import java.util.Objects;
import java.util.stream.Collectors;

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
        List<UserVideoStats> statsList;

        // 获取视频时长
        LocalTime videoDuration = videoRepository.findDurationByVideoId(videoId);
        if (videoDuration == null) {
            return new ArrayList<>();
        }

        // 将 LocalTime 转换为秒数
        long videoDurationSeconds = convertToSeconds(videoDuration);

        switch (option) {
            case 0: // 深度观看：观看次数≥5 或 观看时长≥2倍视频时长
                LocalTime minDuration0 = secondsToLocalTime(videoDurationSeconds * 2);
                statsList = userVideoStatsRepository.findByVideoIdAndMinCountOrDuration(videoId, 5, minDuration0);
                break;
            case 1: // 极其深度观看：观看次数≥10 或 观看时长≥5倍视频时长
                LocalTime minDuration1 = secondsToLocalTime(videoDurationSeconds * 5);
                statsList = userVideoStatsRepository.findByVideoIdAndMinCountOrDuration(videoId, 10, minDuration1);
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
        // 假设我们使用 option 0 的深度观看标准：观看次数≥5 或 观看时长≥2倍视频时长
        final int MIN_WATCH_COUNT = 5;

        // 1. 获取用户所有的观看统计
        List<UserVideoStats> allStats = userVideoStatsRepository.findByUserId(userId);
        if (allStats.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 筛选深度观看视频
        List<DeepVideoRecommendationDTO> deepVideos = allStats.stream()
                .filter(stats -> {
                    Long videoId = stats.getVideoId();
                    LocalTime videoDurationTime = videoRepository.findDurationByVideoId(videoId);
                    if (videoDurationTime == null) {
                        return false;
                    }

                    long videoDurationSeconds = convertToSeconds(videoDurationTime);
                    long minDurationSeconds = videoDurationSeconds * 2; // 2倍视频时长
                    LocalTime minDurationTime = secondsToLocalTime(minDurationSeconds);

                    return stats.getWatchCount() >= MIN_WATCH_COUNT || stats.getTotalWatchDuration().compareTo(minDurationTime) >= 0;
                })
                .map(stats -> {
                    // 获取视频标题
                    String videoTitle = getVideoTitle(stats.getVideoId());
                    // 返回包含用户和视频信息的DTO。由于是查询当前用户，这里User信息固定为当前用户。
                    User user = userRepository.findByUserId(userId);
                    if (user != null) {
                        return new DeepVideoRecommendationDTO(
                                user.getUserId(),
                                user.getUsername(),
                                user.getAvatarPath(),
                                stats.getVideoId(),
                                videoTitle,
                                convertToSeconds(stats.getTotalWatchDuration()),
                                stats.getWatchCount()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return deepVideos;
    }

    /**
     * 转换为DTO
     */
    private List<BaseDTO> convertToDTO(List<UserVideoStats> statsList, Long videoId) {
        // 获取视频标题
        String videoTitle = getVideoTitle(videoId);

        return statsList.stream()
                .map(stats -> {
                    User user = userRepository.findByUserId(stats.getUserId());
                    if (user != null) {
                        return new DeepVideoRecommendationDTO(
                                user.getUserId(),
                                user.getUsername(),
                                user.getAvatarPath(),
                                videoId,
                                videoTitle,
                                convertToSeconds(stats.getTotalWatchDuration()),
                                stats.getWatchCount()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
     * 秒数转换为LocalTime
     */
    private LocalTime secondsToLocalTime(long totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return LocalTime.of(hours, minutes, seconds);
    }

    /**
     * LocalTime转换为秒数
     */
    private int convertToSeconds(LocalTime time) {
        if (time == null) return 0;
        return time.getHour() * 3600 + time.getMinute() * 60 + time.getSecond();
    }
}