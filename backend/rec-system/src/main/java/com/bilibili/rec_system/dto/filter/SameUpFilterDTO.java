package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameUpFilterDTO extends FilterBaseDTO {
    private Long upId;              // UP主ID (必需)
    private Integer durationOption; // 时长筛选条件 (必需)

    public SameUpFilterDTO() {}

    public SameUpFilterDTO(Long upId, Integer durationOption) {
        this.upId = upId;
        this.durationOption = durationOption;
    }

}
