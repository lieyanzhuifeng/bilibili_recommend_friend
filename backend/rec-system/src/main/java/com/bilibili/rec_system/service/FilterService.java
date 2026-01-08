package com.bilibili.rec_system.service;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import java.util.List;
//这里使用了策略模式，这个类是筛选功能的接口，具体实现在其他地方
public interface FilterService {
    List<BaseDTO> filterUsers(FilterBaseDTO filter);
}