package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeriesFilterDTO extends FilterBaseDTO {
    private Long tagId;    // 标签ID

    public SeriesFilterDTO() {}
}