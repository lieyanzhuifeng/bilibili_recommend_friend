package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SameUpVideoCountFilterDTO extends FilterBaseDTO {
    private Long upId;
    private Integer ratioOption;

    public SameUpVideoCountFilterDTO() {}

    public SameUpVideoCountFilterDTO(Long upId, Integer ratioOption) {
        this.upId = upId;
        this.ratioOption = ratioOption;
    }

}