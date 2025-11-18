package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyRecommendationService implements RecommendationService {
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;

    @Override
    public List<User> recommendUsers(Long userId) {
        List<Long> repliedUserIds = commentRepository.findUsersWhoRepliedToUser(userId);
        return userRepository.findByUserIdIn(repliedUserIds);
    }
}
