package com.bilibili.rec_system.service.recommend_filter;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐请求（对应请假条）
 */
@Getter
@Setter
public class Request {
    // 推荐方法标签（对应请假内容）
    private String recommendationType;
    private Integer userActivity = 0;
    private Integer nightOwl = 0;

    private Long UserId;
    private FilterBaseDTO filterBaseDTO;

    private List<BaseDTO> recommendations = new ArrayList<>();


    public Request(String recommendationType, Long UserId, FilterBaseDTO filterBaseDTO,
                   Integer userActivity, Integer nightOwl) {
        if (recommendationType == null) {
            throw new IllegalArgumentException("推荐类型不能为空");
        }
        this.recommendationType = recommendationType;
        this.UserId = UserId;
        this.filterBaseDTO = filterBaseDTO;
        this.userActivity = userActivity != null ? userActivity : 0;
        this.nightOwl = nightOwl != null ? nightOwl : 0;
    }
}