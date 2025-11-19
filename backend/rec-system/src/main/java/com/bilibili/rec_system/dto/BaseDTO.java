
// BaseUserDTO.java
package com.bilibili.rec_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseDTO {
    protected Long userId;
    protected String username;
    protected String avatarPath;

    // 构造方法
    public BaseDTO() {}

    public BaseDTO(Long userId, String username, String avatarPath) {
        this.userId = userId;
        this.username = username;
        this.avatarPath = avatarPath;
    }

}