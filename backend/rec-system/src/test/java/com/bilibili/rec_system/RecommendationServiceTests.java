package com.bilibili.rec_system;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.RecommendationService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class RecommendationServiceTests {

    @Autowired
    private RecommendationServiceFactory recommendationServiceFactory;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserRecommendationsWithRepositoryValidation() {
        System.out.println("=== 测试用户推荐功能（使用工厂方法） ===");

        testUser1Recommendations();
        testUser2Recommendations();
    }

    private void testUser1Recommendations() {
        System.out.println("\n--- 测试用户ID: 1 ---");

        // 1. 回复推荐验证
        testReplyRecommendation(1L);

        // 2. 同视频评论推荐验证
        testCoCommentRecommendation(1L);
    }

    private void testUser2Recommendations() {
        System.out.println("\n--- 测试用户ID: 2 ---");

        // 1. 回复推荐验证
        testReplyRecommendation(2L);

        // 2. 同视频评论推荐验证
        testCoCommentRecommendation(2L);
    }

    private void testReplyRecommendation(Long userId) {
        System.out.println("\n【回复推荐验证 - 用户" + userId + "】");

        // 使用工厂方法获取服务
        RecommendationService service = recommendationServiceFactory.getRecommendationService("reply");
        List<User> serviceResult = service.recommendUsers(userId);
        List<Long> serviceUserIds = serviceResult.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // Repository层直接查询
        List<Long> repositoryUserIds = commentRepository.findUsersWhoRepliedToUser(userId);
        List<User> repositoryUsers = userRepository.findByUserIdIn(repositoryUserIds);
        List<Long> repositoryUserIdsFromUsers = repositoryUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 验证结果一致性
        System.out.println("Service层返回用户ID: " + serviceUserIds);
        System.out.println("Repository层查询用户ID: " + repositoryUserIds);
        System.out.println("Repository层用户对象ID: " + repositoryUserIdsFromUsers);

        boolean serviceRepoConsistent = serviceUserIds.equals(repositoryUserIds);
        boolean repoUserConsistent = repositoryUserIds.equals(repositoryUserIdsFromUsers);

        System.out.println("Service与Repository ID一致: " + serviceRepoConsistent);
        System.out.println("Repository ID与用户对象一致: " + repoUserConsistent);

        if (!serviceRepoConsistent) {
            System.out.println("❌ 错误: Service层与Repository层结果不一致！");
        } else {
            System.out.println("✅ Service层逻辑正确");
        }
    }

    private void testCoCommentRecommendation(Long userId) {
        System.out.println("\n【同视频评论推荐验证 - 用户" + userId + "】");

        // 使用工厂方法获取服务
        RecommendationService service = recommendationServiceFactory.getRecommendationService("co_comment");
        List<User> serviceResult = service.recommendUsers(userId);
        List<Long> serviceUserIds = serviceResult.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // Repository层直接查询（模拟Service内部逻辑）
        List<Long> userVideoIds = commentRepository.findCommentedVideosByUser(userId);
        List<Long> repositoryUserIds = commentRepository.findUsersWhoCommentedOnVideos(userVideoIds, userId);
        List<User> repositoryUsers = userRepository.findByUserIdIn(repositoryUserIds);
        List<Long> repositoryUserIdsFromUsers = repositoryUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 验证结果一致性
        System.out.println("用户评论过的视频ID: " + userVideoIds);
        System.out.println("Service层返回用户ID: " + serviceUserIds);
        System.out.println("Repository层查询用户ID: " + repositoryUserIds);
        System.out.println("Repository层用户对象ID: " + repositoryUserIdsFromUsers);

        boolean serviceRepoConsistent = serviceUserIds.equals(repositoryUserIds);
        boolean repoUserConsistent = repositoryUserIds.equals(repositoryUserIdsFromUsers);

        System.out.println("Service与Repository ID一致: " + serviceRepoConsistent);
        System.out.println("Repository ID与用户对象一致: " + repoUserConsistent);

        if (!serviceRepoConsistent) {
            System.out.println("❌ 错误: Service层与Repository层结果不一致！");
        } else {
            System.out.println("✅ Service层逻辑正确");
        }
    }

}