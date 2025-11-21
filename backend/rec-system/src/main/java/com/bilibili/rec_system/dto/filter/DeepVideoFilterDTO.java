package com.bilibili.rec_system.dto.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeepVideoFilterDTO extends FilterBaseDTO {
    private Long videoId;    // 视频ID
    private Integer option;  // 筛选选项

    public DeepVideoFilterDTO() {}
}