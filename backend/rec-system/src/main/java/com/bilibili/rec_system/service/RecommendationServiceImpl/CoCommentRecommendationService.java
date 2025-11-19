// CoCommentRecommendationService.java
package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.CoCommentRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoCommentRecommendationService implements RecommendationService {
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VideoRepository videoRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        // 1. 获取用户评论过的所有视频ID
        List<Long> userVideoIds = commentRepository.findCommentedVideosByUser(userId);
        if (userVideoIds.isEmpty()) return new ArrayList<>();

        // 2. 获取对应的视频标题列表
        List<String> videoTitles = videoRepository.findTitlesByVideoIdIn(userVideoIds);

        // 3. 安全检查：确保两个列表长度一致
        if (userVideoIds.size() != videoTitles.size()) {
            // 使用最小长度避免越界
            int minSize = Math.min(userVideoIds.size(), videoTitles.size());
            userVideoIds = userVideoIds.subList(0, minSize);
            videoTitles = videoTitles.subList(0, minSize);
        }

        // 4. 为每个视频找到其他评论者，创建用户-视频对
        List<CoCommentRecommendationDTO> result = new ArrayList<>();

        for (int i = 0; i < userVideoIds.size(); i++) {
            Long videoId = userVideoIds.get(i);
            String videoTitle = videoTitles.get(i);

            // 获取在这个视频下评论过的其他用户
            List<Long> otherUserIds = commentRepository.findUsersByVideoIdExcludingUser(videoId, userId);

            // 获取这些用户的详细信息
            List<User> otherUsers = userRepository.findByUserIdIn(otherUserIds);

            // 为每个其他用户创建一个DTO
            for (User user : otherUsers) {
                CoCommentRecommendationDTO dto = new CoCommentRecommendationDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatarPath(),
                        videoTitle
                );
                result.add(dto);
            }
        }

        return new ArrayList<>(result);
    }
}