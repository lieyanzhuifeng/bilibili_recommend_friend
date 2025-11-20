package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.SameUpRecommendationDTO;
import com.bilibili.rec_system.dto.filter.SameUpFilterDTO;
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



}