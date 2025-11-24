package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 通过用户ID获取用户对象
     * @param userId 用户ID
     * @return 用户对象，如果找不到返回null
     */
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * 通过用户ID列表获取用户对象列表
     * @param userIds 用户ID列表
     * @return 用户对象列表
     */
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findByUserIds(userIds);
    }

    /**
     * 通过用户ID列表获取用户对象列表（使用IN查询）
     * @param userIds 用户ID列表
     * @return 用户对象列表
     */
    public List<User> getUsersByIdsIn(List<Long> userIds) {
        return userRepository.findByUserIdIn(userIds);
    }

    /**
     * 根据用户ID列表获取用户名映射
     * @param userIds 用户ID列表
     * @return 用户ID和用户名的映射列表
     */
    public List<Map<String, Object>> getUsernamesByUserIds(List<Long> userIds) {
        return userRepository.findUsernamesByUserIds(userIds);
    }

    /**
     * 获取UP主名称
     * @param upId UP主ID
     * @return UP主名称，如果找不到返回null
     */
    public String getUpNameById(Long upId) {
        return userRepository.findUpNameById(upId);
    }

    /**
     * 批量获取UP主名称
     * @param upIds UP主ID列表
     * @return UP主ID和名称的列表
     */
    public List<Object[]> getUpNamesByIds(List<Long> upIds) {
        return userRepository.findUpNamesByIds(upIds);
    }

    /**
     * 保存或更新用户信息
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * 检查用户是否存在
     * @param userId 用户ID
     * @return 如果用户存在返回true，否则返回false
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 获取所有用户
     * @return 所有用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}