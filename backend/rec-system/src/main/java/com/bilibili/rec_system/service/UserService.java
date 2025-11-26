package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.Video;
import com.bilibili.rec_system.entity.Tag;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TagRepository tagRepository;

    /**
     * 通过用户ID获取用户对象
     * @param userId 用户ID
     * @return 用户对象，如果找不到返回null
     */
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * 根据用户名模糊搜索用户（不区分大小写）
     * @param username 用户名关键词
     * @return 匹配的用户列表
     */
    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContaining(username);
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

    /**
     * 根据标题模糊搜索视频
     */
    public List<Video> searchVideosByTitle(String title) {
        return videoRepository.findByTitleContaining(title);
    }

    /**
     * 根据标签名称模糊搜索标签
     * @param tagName 标签名称关键词
     * @return 匹配的标签列表
     */
    public List<Tag> searchTagsByName(String tagName) {
        return tagRepository.findByTagNameContaining(tagName);
    }
}