package com.bilibili.rec_system;

import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.entity.Comment;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.Video;
import com.bilibili.rec_system.service.CommentBasedFriendRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
@Transactional // 确保测试不会修改真实数据
public class CommentBasedFriendRecommendationServiceRealDBTest {

    @Autowired
    private CommentBasedFriendRecommendationService recommendationService;

    /**
     * 测试真实用户 - 使用你数据库中存在的用户ID
     */
    @Test
    public void testWithUserCommentComparison() {
        Long userId = 1L;

        System.out.println("=== 包含用户评论对照的推荐测试 ===");

        List<FriendRecommendationDTO> results = recommendationService.recommendFriendsByComments(userId);

        if (results.isEmpty()) {
            System.out.println("❌ 没有找到匹配的好友推荐");
        } else {
            System.out.println("✅ 找到 " + results.size() + " 个推荐好友:");
            System.out.println("=====================================\n");

            for (int i = 0; i < results.size(); i++) {
                FriendRecommendationDTO result = results.get(i);
                System.out.println("【推荐 " + (i + 1) + "】");
                displayRecommendationWithComparison(result);
                System.out.println("-------------------------------------\n");
            }
        }
    }

    /**
     * 显示包含用户评论对照的推荐结果
     */
    private void displayRecommendationWithComparison(FriendRecommendationDTO result) {
        // 用户自己的评论
        Comment userComment = result.getUserComment();
        System.out.println("👤 我的评论:");
        System.out.println("   评论ID: " + userComment.getCommentId());
        System.out.println("   内容: " + (userComment.getContent() != null ?
                userComment.getContent() : "null"));
        System.out.println("   视频ID: " + userComment.getVideoId());

        // 匹配到的评论
        Comment matchedComment = result.getMatchedComment();
        System.out.println("\n💬 匹配到的评论:");
        System.out.println("   评论ID: " + matchedComment.getCommentId());
        System.out.println("   内容: " + (matchedComment.getContent() != null ?
                matchedComment.getContent() : "null"));
        System.out.println("   用户ID: " + matchedComment.getUserId());

        // 推荐用户信息
        User recommendedUser = result.getRecommendedUser();
        System.out.println("\n🎯 推荐用户:");
        System.out.println("   ID: " + recommendedUser.getUserId());
        System.out.println("   用户名: " + recommendedUser.getUsername());
        System.out.println("   注册时间: " + recommendedUser.getRegisterTime());

        // 视频信息
        Video video = result.getVideo();
        System.out.println("\n🎬 相关视频:");
        System.out.println("   ID: " + video.getVideoId());
        System.out.println("   标题: " + video.getTitle());
        System.out.println("   分类: " + video.getCategoryId());

        // 匹配信息
        System.out.println("\n⭐ 匹配信息:");
        System.out.println("   匹配分数: " + String.format("%.4f", result.getMatchScore()));
        System.out.println("   匹配等级: " + getMatchLevel(result.getMatchScore()));

        // 评论长度对比
        int userCommentLength = userComment.getContent() != null ? userComment.getContent().length() : 0;
        int matchedCommentLength = matchedComment.getContent() != null ? matchedComment.getContent().length() : 0;
        System.out.println("   评论长度对比: 我的评论 " + userCommentLength + " 字 vs 匹配评论 " + matchedCommentLength + " 字");
    }

    /**
     * 根据匹配分数获取等级描述
     */
    private String getMatchLevel(double score) {
        if (score >= 0.9) return "🌟 极高匹配";
        if (score >= 0.8) return "⭐ 高匹配";
        if (score >= 0.7) return "✅ 中等匹配";
        if (score >= 0.6) return "⚠️ 低匹配";
        return "❌ 不匹配";
    }

    /**
     * 测试数据库连接和基础功能
     */
    @Test
    public void testServiceHealth() {
        System.out.println("=== 服务健康检查 ===");

        try {
            // 测试配置
            int minLength = recommendationService.getMinCommentLength();
            double threshold = recommendationService.getMatchScoreThreshold();

            System.out.println("✅ 服务配置正常:");
            System.out.println("   最小评论字数: " + minLength);
            System.out.println("   匹配分数阈值: " + threshold);

            // 测试一个简单用户
            Long testUserId = 1L; // 使用你的最小用户ID
            System.out.println("\n🔍 测试用户 " + testUserId + " 的基础推荐...");

            List<FriendRecommendationDTO> results = recommendationService.recommendFriendsByComments(testUserId);
            System.out.println("   推荐结果数量: " + results.size());
            System.out.println("✅ 服务运行正常");

        } catch (Exception e) {
            System.out.println("❌ 服务异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

}