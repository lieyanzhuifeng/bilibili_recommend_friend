package com.bilibili.rec_system;

import com.bilibili.rec_system.dto.*;
import com.bilibili.rec_system.dto.filter.*;
import com.bilibili.rec_system.entity.*;
import com.bilibili.rec_system.repository.*;
import com.bilibili.rec_system.service.*;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import com.bilibili.rec_system.service.FilterServiceImpl.FilterServiceFactory;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class RecommendationServiceTests {

    @Autowired
    private FilterServiceFactory filterServiceFactory;

    @Autowired
    private RecommendationServiceFactory recommendationServiceFactory;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTopThemeRepository userTopThemeRepository;

    @Autowired
    private UserTopCategoryRepository userTopCategoryRepository;

    @Autowired
    private VideoThemeRepository videoThemeRepository;

    @Autowired
    private UserFollowUpRepository userFollowUpRepository;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

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

    //观看视频相似度
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

    //最喜欢的分区重合度
    @Test
    void testCategoryRecommendationJson() {
        System.out.println("=== 分区重合度推荐测试 (用户1-10) ===\n");

        RecommendationService service = recommendationServiceFactory.getRecommendationService("category");
        ObjectMapper mapper = new ObjectMapper();

        for (long userId = 1; userId <= 10; userId++) {
            testUserCategoryJson(userId, service, mapper);
        }
    }

    private void testUserCategoryJson(Long userId, RecommendationService service, ObjectMapper mapper) {
        try {
            List<BaseDTO> baseResult = service.recommendUsers(userId);
            List<CategoryRecommendationDTO> dtoResult = baseResult.stream()
                    .map(dto -> (CategoryRecommendationDTO) dto)
                    .collect(Collectors.toList());

            // 使用分区相关的Repository，不是主题相关的！
            List<UserTopCategory> userCategories = userTopCategoryRepository.findTop3ByUserId(userId);

            // 构建简洁的测试结果对象
            Map<String, Object> testResult = new LinkedHashMap<>();
            testResult.put("userId", userId);
            testResult.put("categoryCount", userCategories.size());
            testResult.put("recommendationCount", dtoResult.size());

            if (!dtoResult.isEmpty()) {
                testResult.put("recommendations", dtoResult);
            }

            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testResult);
            System.out.println(jsonResult);
            System.out.println(); // 空行分隔

        } catch (Exception e) {
            System.out.println("用户 " + userId + " 测试失败: " + e.getMessage());
        }
    }

    //最喜欢的内容类型重合度
    @Test
    void testThemeRecommendationForAllUsers() {
        System.out.println("=== 测试1-10号用户的内容类型重合度推荐 ===");

        RecommendationService service = recommendationServiceFactory.getRecommendationService("theme");

        for (long userId = 1; userId <= 10; userId++) {
            testSingleUserThemeRecommendation(userId, service);
        }
    }

    private void testSingleUserThemeRecommendation(Long userId, RecommendationService service) {
        System.out.println("\n--- 测试用户ID: " + userId + " ---");

        // 获取推荐结果
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 向下转型为具体类型
        List<ThemeRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (ThemeRecommendationDTO) dto)
                .collect(Collectors.toList());

        // 使用JSON格式打印结果
        System.out.println("内容类型重合度推荐结果: ");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(dtoResult);
        }

        System.out.println("推荐用户数量: " + dtoResult.size());

        // 这里可以添加主题相关的验证（如果需要）
        List<UserTopTheme> userThemes = userTopThemeRepository.findTop3ByUserId(userId);
        System.out.println("用户" + userId + "的主题偏好数量: " + userThemes.size());
    }

    //输入upID和选项 筛选符合条件的用户及他们观看该up的总时长
    @Test
    void testSameUpFilter() {
        System.out.println("=== 测试同一UP主筛选功能 ===");

        FilterService filterService = filterServiceFactory.getFilterService("same_up");

        // 测试UP主ID为11，测试所有时长选项
        for (int option = 0; option <= 4; option++) {
            testSameUpFilterWithOption(11L, option, filterService);
        }
    }

    private void testSameUpFilterWithOption(Long upId, Integer option, FilterService filterService) {
        SameUpFilterDTO filter = new SameUpFilterDTO(upId, option);

        List<BaseDTO> baseResult = filterService.filterUsers(filter);
        List<SameUpRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (SameUpRecommendationDTO) dto)
                .collect(Collectors.toList());

        String optionDesc = getOptionDescription(option);
        System.out.println("\nUP主" + upId + " - 选项[" + optionDesc + "]: " + dtoResult.size() + "个用户");

        // JSON格式打印结果
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.err.println("JSON转换失败: " + e.getMessage());
            // 降级显示
            System.out.println("原始对象: " + dtoResult);
        }
    }

    private String getOptionDescription(Integer option) {
        switch (option) {
            case -1: return "全部";
            case 0: return "0-1小时";
            case 1: return "1-3小时";
            case 2: return "3-10小时";
            case 3: return "10-30小时";
            case 4: return "30小时以上";
            default: return "未知";
        }
    }

    //输入tagID和选项 输出符合条件的用户及他们观看该tag的总时长
    @Test
    void testSameTagRecommendation() {
        System.out.println("=== 测试同一标签推荐功能 ===");

        FilterService service = filterServiceFactory.getFilterService("same_tag");

        // 测试不同标签
        testTagRecommendation(1L, service);
    }

    private void testTagRecommendation(Long tagId, FilterService service) {
        System.out.println("\n--- 测试标签ID: " + tagId + " ---");

        // 直接使用常量，避免类型转换
        Map<Integer, String> durationLevels = Map.of(
                -1, "全部",
                0, "0-1小时",
                1, "1-3小时",
                2, "3-10小时",
                3, "10-30小时",
                4, "30小时以上"
        );

        for (int option = -1; option <= 4; option++) {
            SameTagFilterDTO filter = new SameTagFilterDTO(tagId, option);
            List<BaseDTO> baseResult = service.filterUsers(filter);
            List<SameTagRecommendationDTO> dtoResult = baseResult.stream()
                    .map(dto -> (SameTagRecommendationDTO) dto)
                    .collect(Collectors.toList());

            String optionDesc = durationLevels.get(option);
            System.out.println("标签" + tagId + " - 时长等级[" + optionDesc + "]: " + dtoResult.size() + "个推荐用户");

            if (!dtoResult.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(dtoResult.subList(0, Math.min(3, dtoResult.size())));
                    System.out.println(jsonResult);
                } catch (Exception e) {
                    System.out.println(dtoResult.subList(0, Math.min(3, dtoResult.size())));
                }
            }
        }
    }


    //输入upID和选项 筛选符合条件的用户及他们观看该up的视频个数
    @Test
    void testSameUpVideoCountForUp11() {
        System.out.println("=== 测试UP主11的视频观看比例筛选 ===");

        FilterService service = filterServiceFactory.getFilterService("same_up_video_count");

        // 直接使用常量
        Map<Integer, String> ratioLevels = Map.of(
                0, "0-20%", 1, "20-40%", 2, "40-60%", 3, "60-80%", 4, "80-100%"
        );

        for (int option = 0; option <= 4; option++) {
            SameUpVideoCountFilterDTO filter = new SameUpVideoCountFilterDTO(11L, option);
            List<BaseDTO> baseResult = service.filterUsers(filter);
            List<SameUpVideoCountDTO> dtoResult = baseResult.stream()
                    .map(dto -> (SameUpVideoCountDTO) dto)
                    .collect(Collectors.toList());

            String optionDesc = ratioLevels.get(option);
            System.out.println("\nUP主11 - 观看比例[" + optionDesc + "]: " + dtoResult.size() + "个推荐用户");

            if (!dtoResult.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
                    System.out.println(jsonResult);
                } catch (Exception e) {
                    System.out.println(dtoResult);
                }
            }
        }
    }

    //输入tagID和选项 筛选符合条件的用户及他们观看该tag的视频个数
    @Test
    void testSameTagVideoCountForTag1() {
        System.out.println("=== 测试标签1的视频观看比例筛选 ===");

        FilterService service = filterServiceFactory.getFilterService("same_tag_video_count");

        // 测试所有比例选项
        for (int option = 0; option <= 4; option++) {
            SameTagVideoCountFilterDTO filter = new SameTagVideoCountFilterDTO(1L, option);
            List<BaseDTO> baseResult = service.filterUsers(filter);
            List<SameTagVideoCountDTO> dtoResult = baseResult.stream()
                    .map(dto -> (SameTagVideoCountDTO) dto)
                    .collect(Collectors.toList());

            String optionDesc = getRatioDescription(option);
            System.out.println("\n标签1 - 观看比例[" + optionDesc + "]: " + dtoResult.size() + "个推荐用户");

            if (!dtoResult.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
                    System.out.println(jsonResult);
                } catch (Exception e) {
                    System.out.println(dtoResult);
                }
            }
        }
    }

    private String getRatioDescription(int option) {
        return switch (option) {
            case 0 -> "0-20%";
            case 1 -> "20-40%";
            case 2 -> "40-60%";
            case 3 -> "60-80%";
            case 4 -> "80-100%";
            default -> "未知";
        };
    }

    //输入用户id（前端需要保证只能输入本登录用户的id）和upid返回关注该up时间相近的用户
    @Test
    void testFollowTimeRecommendation() {
        System.out.println("=== 测试关注时间缘分推荐功能 ===");

        Long upId = 11L;

        for (long userId = 1; userId <= 10; userId++) {
            testSingleUserFollowTimeRecommendation(userId, upId);
        }
    }

    private void testSingleUserFollowTimeRecommendation(Long userId, Long upId) {
        System.out.println("\n--- 测试用户ID: " + userId + " 对UP主ID: " + upId + " ---");

        // 使用工厂获取服务
        FilterService service = filterServiceFactory.getFilterService("follow_time");

        // 创建过滤器
        FollowTimeFilterDTO filter = new FollowTimeFilterDTO(userId, upId);

        // 调用服务
        List<BaseDTO> baseResult = service.filterUsers(filter);

        // 向下转型为具体类型
        List<FollowTimeRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (FollowTimeRecommendationDTO) dto)
                .collect(Collectors.toList());

        // 使用JSON格式打印结果
        System.out.println("关注时间缘分推荐结果: ");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(dtoResult);
        }

        System.out.println("推荐用户数量: " + dtoResult.size());

        // 显示统计信息
        if (!dtoResult.isEmpty()) {
            double avgScore = dtoResult.stream()
                    .mapToInt(FollowTimeRecommendationDTO::getRecommendationScore)
                    .average()
                    .orElse(0.0);
            System.out.println("平均推荐分数: " + String.format("%.2f", avgScore));

            // 显示前3个最高分用户
            System.out.println("最高分推荐用户:");
            for (int i = 0; i < Math.min(3, dtoResult.size()); i++) {
                FollowTimeRecommendationDTO dto = dtoResult.get(i);
                System.out.println("  " + (i+1) + ". 用户" + dto.getUserId() + " - 分数:" + dto.getRecommendationScore());
                System.out.println("     理由: " + dto.getRecommendationReason());
            }
        } else {
            System.out.println("❌ 没有找到有缘分的用户");
        }

        // 验证当前用户是否关注了该UP主
        Integer userFollowDays = userFollowUpRepository.findFollowDaysByUserAndUP(userId, upId);
        if (userFollowDays != null) {
            System.out.println("用户" + userId + "已关注该UP主 " + userFollowDays + " 天");
        } else {
            System.out.println("⚠️ 用户" + userId + "未关注该UP主");
        }
    }

    //输入userid推荐看视频习惯（完播率收藏率等等）的相似度
    @Test
    void testUserBehaviorRecommendationForUsers1To10() {
        System.out.println("=== 测试1-10号用户的行为相似度推荐 ===\n");

        for (long userId = 1; userId <= 10; userId++) {
            testUserBehaviorForUser(userId);
        }
    }

    private void testUserBehaviorForUser(Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("user_behavior");
        List<BaseDTO> baseResult = service.recommendUsers(userId);
        List<UserBehaviorRecommendationDTO> dtoResult = baseResult.stream()
                .map(dto -> (UserBehaviorRecommendationDTO) dto)
                .collect(Collectors.toList());

        System.out.println("用户" + userId + "的推荐结果: ");

        // 直接打印DTO
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
            System.out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(dtoResult);
        }

        System.out.println("推荐用户数量: " + dtoResult.size());
        System.out.println();
    }

    //输入option根据夜间看视频情况筛选用户
    @Test
    void testNightOwlRecommendation() {
        System.out.println("=== 测试夜猫子筛选 ===");

        String[] levels = {"non_owl", "light_owl", "medium_owl", "heavy_owl"};

        for (String level : levels) {
            FilterService service = filterServiceFactory.getFilterService("night_owl");
            NightOwlFilterDTO filter = new NightOwlFilterDTO(level);
            List<BaseDTO> result = service.filterUsers(filter);

            System.out.println(level + ": " + result.size() + "个用户");

            // 使用JSON格式打印
            if (!result.isEmpty()) {
                List<NightOwlRecommendationDTO> dtoResult = result.stream()
                        .map(dto -> (NightOwlRecommendationDTO) dto)
                        .collect(Collectors.toList());

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
                    System.out.println(json);
                } catch (Exception e) {
                    System.out.println(dtoResult);
                }
            }
            System.out.println();
        }
    }

    //输入option根据夜间看视频情况筛选轻度中度重度b站使用用户
    @Test
    void testUserActivityRecommendation() {
        System.out.println("=== 测试用户活跃度筛选 ===");

        String[] levels = {"light_user", "medium_user", "heavy_user"};

        for (String level : levels) {
            FilterService service = filterServiceFactory.getFilterService("user_activity");
            UserActivityFilterDTO filter = new UserActivityFilterDTO(level);
            List<BaseDTO> result = service.filterUsers(filter);

            System.out.println(level + ": " + result.size() + "个用户");

            // 转换为具体类型并打印JSON
            if (!result.isEmpty()) {
                List<UserActivityRecommendationDTO> dtoResult = result.stream()
                        .map(dto -> (UserActivityRecommendationDTO) dto)
                        .collect(Collectors.toList());

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoResult);
                    System.out.println(json);
                } catch (Exception e) {
                    System.out.println(dtoResult);
                }
            }
            System.out.println();
        }
    }





}