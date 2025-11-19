package com.bilibili.rec_system;

import com.bilibili.rec_system.dto.SharedVideoRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.CoCommentRecommendationDTO;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.RecommendationService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.CoCommentRecommendationService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import com.bilibili.rec_system.service.RecommendationServiceImpl.SharedVideoRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    //在同一个视频下评论
    @Test
    void testCoCommentRecommendation() {
        System.out.println("=== 测试同视频评论推荐功能 ===");

        // 分别测试用户1和用户2
        testUserCoComment(1L);
        testUserCoComment(2L);
    }

    private void testUserCoComment(Long userId) {
        System.out.println("\n--- 测试用户ID: " + userId + " ---");

        RecommendationService service = recommendationServiceFactory.getRecommendationService("co_comment");

        // 接口返回 BaseDTO，但实际是 CoCommentRecommendationDTO
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 向下转型为具体类型
        List<CoCommentRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (CoCommentRecommendationDTO) dto)
                .collect(Collectors.toList());

        // 使用JSON格式打印结果
        System.out.println("同视频评论推荐结果: ");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            // 如果JSON转换失败，使用默认的toString
            System.out.println(dtoResult);
        }

        // 验证数据
        List<Long> userVideoIds = commentRepository.findCommentedVideosByUser(userId);
        List<Long> repositoryUserIds = commentRepository.findUsersWhoCommentedOnVideos(userVideoIds, userId);
        System.out.println("用户" + userId + "评论过的视频数量: " + userVideoIds.size());
        System.out.println("共同评论用户数量: " + repositoryUserIds.size());
        System.out.println("返回DTO数量: " + dtoResult.size());

        // 修改这里：显示每个推荐项的视频标题
        if (!dtoResult.isEmpty()) {
            System.out.println("推荐结果详情:");
            for (int i = 0; i < Math.min(3, dtoResult.size()); i++) {
                CoCommentRecommendationDTO dto = dtoResult.get(i);
                System.out.println("  " + (i+1) + ". 用户" + dto.getUserId() +
                        " - 共同视频: " + dto.getVideoTitle());
            }

            // 统计不同视频的数量
            long uniqueVideoCount = dtoResult.stream()
                    .map(CoCommentRecommendationDTO::getVideoTitle)
                    .distinct()
                    .count();
            System.out.println("涉及的不同视频数量: " + uniqueVideoCount);
        }
    }

    //回复过评论
    @Test
    void testReplyRecommendationForMultipleUsers() {
        System.out.println("=== 测试回复推荐功能（用户1-10） ===");

        for (long userId = 1; userId <= 10; userId++) {
            testReplyRecommendationForUser(userId);
        }
    }

    private void testReplyRecommendationForUser(Long userId) {
        System.out.println("\n--- 测试用户ID: " + userId + " ---");

        RecommendationService service = recommendationServiceFactory.getRecommendationService("reply");

        // 接口返回 BaseDTO
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 由于返回的是 BaseDTO，不需要向下转型
        System.out.println("回复推荐结果: ");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(baseResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(baseResult);
        }

        // 验证数据
        List<Long> repliedUserIds = commentRepository.findUsersWhoRepliedToUser(userId);
        System.out.println("回复过用户" + userId + "的用户数量: " + repliedUserIds.size());
        System.out.println("返回DTO数量: " + baseResult.size());

        // 显示推荐用户详情
        if (!baseResult.isEmpty()) {
            System.out.println("推荐用户详情:");
            for (int i = 0; i < Math.min(3, baseResult.size()); i++) {
                BaseDTO dto = baseResult.get(i);
                System.out.println("  " + (i+1) + ". 用户" + dto.getUserId() + " - " + dto.getUsername());
            }
        } else {
            System.out.println("  暂无回复推荐");
        }

        // 验证数据一致性
        boolean dataConsistent = baseResult.size() == repliedUserIds.size();
        System.out.println("数据一致性: " + (dataConsistent ? "✅ 一致" : "❌ 不一致"));
    }

    @Test
    void testSharedVideoRecommendationForAllUsers() {
        System.out.println("=== 测试1-10号用户的视频相似度推荐 ===");

        RecommendationService service = recommendationServiceFactory.getRecommendationService("shared_video_recommendation");

        for (long userId = 1; userId <= 10; userId++) {
            testSingleUserSharedVideoRecommendation(userId, service);
        }
    }

    private void testSingleUserSharedVideoRecommendation(Long userId, RecommendationService service) {
        System.out.println("\n--- 测试用户ID: " + userId + " ---");

        List<BaseDTO> baseResult = service.recommendUsers(userId);
        List<SharedVideoRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (SharedVideoRecommendationDTO) dto)
                .collect(Collectors.toList());

        // 使用JSON格式打印结果
        System.out.println("视频相似度推荐结果: ");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(dtoResult);
        }

        System.out.println("推荐用户数量: " + dtoResult.size());

    }
}