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




}