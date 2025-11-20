package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivityFilterDTO extends FilterBaseDTO {
    private String option; // 筛选类型：light_user, medium_user, heavy_user

    public UserActivityFilterDTO() {}

    public UserActivityFilterDTO(String option) {
        this.option = option;
    }
}