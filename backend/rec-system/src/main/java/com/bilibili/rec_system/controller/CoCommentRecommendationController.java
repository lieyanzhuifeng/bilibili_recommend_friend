// CoCommentRecommendationController.java
package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.CoCommentRecommendationDTO;
import com.bilibili.rec_system.dto.SharedVideoRecommendationDTO;
import com.bilibili.rec_system.service.RecommendationService;
import com.bilibili.rec_system.service.RecommendationServiceImpl.RecommendationServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*") // 允许所有跨域请求，方便测试
public class CoCommentRecommendationController {

    @Autowired
    private RecommendationServiceFactory recommendationServiceFactory;

    /**
     * 获取同视频评论推荐用户
     */
    @GetMapping("/co-comment/{userId}")
    public List<CoCommentRecommendationDTO> getCoCommentRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("co_comment");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 转换为具体DTO类型
        return baseResult.stream()
                .map(dto -> (CoCommentRecommendationDTO) dto)
                .collect(Collectors.toList());
    }

    /**
     * 获取回复推荐用户
     */
    @GetMapping("/reply/{userId}")
    public List<BaseDTO> getReplyRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("reply");
        // 直接返回BaseDTO，不需要向下转型
        return service.recommendUsers(userId);
    }

    /**
     * 获取观看视频相似度推荐用户
     */
    @GetMapping("/shared-video/{userId}")
    public List<SharedVideoRecommendationDTO> getSharedVideoRecommendations(@PathVariable Long userId) {
        RecommendationService service = recommendationServiceFactory.getRecommendationService("shared_video_recommendation");
        List<BaseDTO> baseResult = service.recommendUsers(userId);

        // 转换为具体DTO类型
        return baseResult.stream()
                .map(dto -> (SharedVideoRecommendationDTO) dto)
                .collect(Collectors.toList());
    }


}
