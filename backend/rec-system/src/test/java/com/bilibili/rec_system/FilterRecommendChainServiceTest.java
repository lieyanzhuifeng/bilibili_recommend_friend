package com.bilibili.rec_system;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.service.recommend_filter.FilterRecommendChainService;
import com.bilibili.rec_system.service.recommend_filter.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class FilterRecommendChainServiceTest {

    @Autowired
    private FilterRecommendChainService chainService;

    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void testFullFilterChain() {
        System.out.println("=== 测试完整责任链：user_behavior 推荐 + 活跃度筛选(2) + 夜猫子筛选(3) ===");

        // 设置参数
        String recommendationType = "user_behavior";  // 推荐类型
        Long userId = 1L;                             // 用户ID
        Integer activityOption = 6;                   // 活跃度筛选选项
        Integer nightOwlOption = 14;                   // 夜猫子筛选选项

        System.out.println("参数: 用户ID=" + userId +
                ", 推荐类型=" + recommendationType +
                ", 活跃度=" + activityOption +
                ", 夜猫子=" + nightOwlOption);

        try {
            // 创建Request对象
            Request request = new Request(
                    recommendationType,  // 推荐类型
                    userId,             // 用户ID
                    null,              // filterBaseDTO不设置
                    activityOption,    // 活跃度选项
                    nightOwlOption     // 夜猫子选项
            );

            // 执行责任链
            List<BaseDTO> results = chainService.executeFullChain(request);

            System.out.println("推荐结果数量: " + results.size());

            // JSON格式打印全部结果
            if (!results.isEmpty()) {
                String jsonResult = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(results);
                System.out.println("推荐结果: ");
                System.out.println(jsonResult);
            } else {
                System.out.println("无推荐结果");
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}