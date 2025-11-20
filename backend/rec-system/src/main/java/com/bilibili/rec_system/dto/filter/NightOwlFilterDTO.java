package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NightOwlFilterDTO extends FilterBaseDTO {
    private String option; // 筛选类型：non_owl, light_owl, medium_owl, heavy_owl

    public NightOwlFilterDTO() {}

    public NightOwlFilterDTO(String option) {
        this.option = option;
    }
}