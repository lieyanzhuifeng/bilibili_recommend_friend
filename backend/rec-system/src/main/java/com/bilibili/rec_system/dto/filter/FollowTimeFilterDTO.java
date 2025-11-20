package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowTimeFilterDTO extends FilterBaseDTO {
    private Long userId;  // 当前用户ID（前端传入）
    private Long upId;    // UP主ID

    public FollowTimeFilterDTO() {}

    public FollowTimeFilterDTO(Long userId, Long upId) {
        this.userId = userId;
        this.upId = upId;
    }
}