package com.bilibili.rec_system.service;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.filter.FilterBaseDTO;
import java.util.List;

public interface FilterService {
    List<BaseDTO> filterUsers(FilterBaseDTO filter);
}