
// BaseUserDTO.java
package com.bilibili.rec_system.dto;

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

    // getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
}