package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameTagFilterDTO extends FilterBaseDTO {
    // getters and setters
    private Long tagId;              // 标签ID
    private Integer durationOption;  // 时长筛选条件

    public SameTagFilterDTO() {}

    public SameTagFilterDTO(Long tagId, Integer durationOption) {
        this.tagId = tagId;
        this.durationOption = durationOption;
    }

}
