package com.bilibili.rec_system;

import com.bilibili.rec_system.service.FilterServiceImpl.FollowTimeRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UpRecommendTest {

    @Autowired
    private FollowTimeRecommendationService followTimeRecommendationService;

    @Test
    public void testRecommendUsers_User1_Up83() {
        System.out.println("开始测试：userId=1, upId=83");

        // 调用实际的service方法
        var result = followTimeRecommendationService.recommendUsers(1L, 83L);

        System.out.println("测试结果：");
        System.out.println("找到 " + result.size() + " 个推荐用户");

        for (var dto : result) {
            var followTimeDTO = (com.bilibili.rec_system.dto.FollowTimeRecommendationDTO) dto;
            System.out.println("用户ID: " + followTimeDTO.getUserId());
            System.out.println("用户名: " + followTimeDTO.getUsername());
            System.out.println("推荐理由: " + followTimeDTO.getRecommendationReason());
            System.out.println("推荐分数: " + followTimeDTO.getRecommendationScore());
            System.out.println("---");
        }
    }
}