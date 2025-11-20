package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameTagVideoCountFilterDTO extends FilterBaseDTO {
    // getters and setters
    private Long tagId;              // 标签ID (必需)
    private Integer ratioOption;     // 观看比例选项 (必需)

    public SameTagVideoCountFilterDTO() {}

    public SameTagVideoCountFilterDTO(Long tagId, Integer ratioOption) {
        this.tagId = tagId;
        this.ratioOption = ratioOption;
    }

}