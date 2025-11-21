package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.dto.*;
import com.bilibili.rec_system.dto.filter.*;
import com.bilibili.rec_system.service.FilterService;
import com.bilibili.rec_system.service.FilterServiceImpl.FilterServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/filter")
@CrossOrigin(origins = "*")
public class FilterController {

    @Autowired
    private FilterServiceFactory filterServiceFactory;

    /**
     * 同一UP主筛选推荐
     */

    @GetMapping("/same-up")
    public List<SameUpRecommendationDTO> filterSameUpUsersByGet(
            @RequestParam Long upId,
            @RequestParam(required = false, defaultValue = "-1") Integer durationOption) {

        SameUpFilterDTO filter = new SameUpFilterDTO(upId, durationOption);
        FilterService service = filterServiceFactory.getFilterService("same_up");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (SameUpRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 同一标签筛选 - GET版本
     */
    @GetMapping("/same-tag")
    public List<SameTagRecommendationDTO> filterSameTagUsers(
            @RequestParam Long tagId,
            @RequestParam(required = false, defaultValue = "-1") Integer durationOption) {

        SameTagFilterDTO filter = new SameTagFilterDTO(tagId, durationOption);
        FilterService service = filterServiceFactory.getFilterService("same_tag");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (SameTagRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * UP主视频观看比例筛选
     */
    @PostMapping("/same-up-video-count")
    public List<SameUpVideoCountDTO> getSameUpVideoCountRecommendations(@RequestBody SameUpVideoCountFilterDTO filter) {
        FilterService service = filterServiceFactory.getFilterService("same_up_video_count");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (SameUpVideoCountDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 标签视频观看比例筛选
     */
    @GetMapping("/same-tag-video-count")
    public List<SameTagVideoCountDTO> getSameTagVideoCountRecommendations(
            @RequestParam Long tagId,
            @RequestParam Integer ratioOption) {
        SameTagVideoCountFilterDTO filter = new SameTagVideoCountFilterDTO(tagId, ratioOption);
        FilterService service = filterServiceFactory.getFilterService("same_tag_video_count");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (SameTagVideoCountDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 关注时间缘分推荐
     */
    @GetMapping("/follow-time")
    public List<FollowTimeRecommendationDTO> getFollowTimeRecommendations(
            @RequestParam Long userId,
            @RequestParam Long upId) {

        FollowTimeFilterDTO filter = new FollowTimeFilterDTO(userId, upId);
        FilterService service = filterServiceFactory.getFilterService("follow_time");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (FollowTimeRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 夜猫子用户筛选
     */
    @GetMapping("/night-owl")
    public List<NightOwlRecommendationDTO> getNightOwlRecommendations(
            @RequestParam String option) {

        NightOwlFilterDTO filter = new NightOwlFilterDTO(option);
        FilterService service = filterServiceFactory.getFilterService("night_owl");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (NightOwlRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 用户活跃度筛选
     */
    @GetMapping("/user-activity")
    public List<UserActivityRecommendationDTO> getUserActivityRecommendations(
            @RequestParam String option) {

        UserActivityFilterDTO filter = new UserActivityFilterDTO(option);
        FilterService service = filterServiceFactory.getFilterService("user_activity");
        List<BaseDTO> baseResult = service.filterUsers(filter);

        return baseResult.stream()
                .map(dto -> (UserActivityRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 深度视频筛选
     */
    @GetMapping("/deep-video")
    public List<DeepVideoRecommendationDTO> deepVideoFilter(
            @RequestParam Long videoId,
            @RequestParam Integer option) {

        FilterService service = filterServiceFactory.getFilterService("deep_video");
        DeepVideoFilterDTO filter = new DeepVideoFilterDTO();
        filter.setVideoId(videoId);
        filter.setOption(option);

        List<BaseDTO> baseResult = service.filterUsers(filter);
        return baseResult.stream()
                .map(dto -> (DeepVideoRecommendationDTO) dto)
                .collect(Collectors.toList());
    }


}