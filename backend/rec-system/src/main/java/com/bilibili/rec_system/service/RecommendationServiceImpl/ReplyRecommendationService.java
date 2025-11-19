package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.ReplyRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReplyRecommendationService implements RecommendationService {
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        List<Long> repliedUserIds = commentRepository.findUsersWhoRepliedToUser(userId);
        List<User> users = userRepository.findByUserIdIn(repliedUserIds);

        // 转换为 BaseDTO（暂时先用 BaseDTO，等具体DTO写好后再用 ReplyRecommendationDTO）
        return users.stream()
                .map(user -> new BaseDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatarPath()
                ))
                .collect(Collectors.toList());
    }

    // 等 ReplyRecommendationDTO 写好后可以添加这个方法
    /*
    public List<ReplyRecommendationDTO> recommendUsersWithDetails(Long userId) {
        List<BaseDTO> baseResult = recommendUsers(userId);
        return baseResult.stream()
                .map(dto -> (ReplyRecommendationDTO) dto)
                .collect(Collectors.toList());
    }
    */
}