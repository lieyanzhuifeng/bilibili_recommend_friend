package com.bilibili.rec_system.service;

import ai.djl.translate.TranslateException;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.entity.Comment;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.Video;
import com.bilibili.rec_system.repository.CommentRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.repository.VideoRepository;
import com.bilibili.rec_system.service.CommentBasedFriendRecommendationService;
import com.bilibili.rec_system.service.content.CommentSimilarityWithSentiment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
//没有使用adapter时方法的具体实现
@Slf4j
@Service
public class CommentBasedFriendRecommendationServiceImpl implements CommentBasedFriendRecommendationService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    // 配置参数
    private static final int MIN_COMMENT_LENGTH = 100; //评论最小长度
    private static final double MATCH_SCORE_THRESHOLD = 0.7; //匹配分数门槛
    private static final int MAX_COMMENTS_PER_VIDEO = 50; //一个视频最多参与匹配的评论数
    private static final int MAX_COMMENTS_PER_PERSON = 10; //一个用户最多参与匹配的评论数
    private static final int MAX_RESULTS = 10; //匹配结果最大值
    private static final int TIMEOUT_SECONDS = 30; //超时时间
    //private static final int BATCH_SIZE = 20;

    private final ExecutorService executorService;

    public CommentBasedFriendRecommendationServiceImpl(CommentRepository commentRepository,
                                                       UserRepository userRepository,
                                                       VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.executorService = Executors.newCachedThreadPool();
    }

    @PostConstruct
    public void init() throws Exception {
        CommentSimilarityWithSentiment.INSTANCE.init();
        log.info("CommentSimilarityWithSentiment 单例初始化完成，提前终止阈值: {}",
                CommentSimilarityWithSentiment.INSTANCE.getEarlyTerminationThreshold());
    }

    @PreDestroy
    public void cleanup() {
        CommentSimilarityWithSentiment.INSTANCE.close();
        if (executorService != null) {
            executorService.shutdown();
            log.info("线程池已关闭");
        }
        log.info("CommentBasedFriendRecommendationService 资源清理完成");
    }

    @Override
    public List<FriendRecommendationDTO> recommendFriendsByComments(Long userId) {
        log.info("开始为用户 {} 生成好友推荐", userId);
        long startTime = System.currentTimeMillis();

        List<FriendRecommendationDTO> results = new ArrayList<>();

        try {
            // 1. 获取用户的所有评论
            List<Comment> userComments = commentRepository.findByUserId(userId);
            log.info("用户 {} 有 {} 条评论", userId, userComments.size());

            // 2. 筛选字数大于阈值的评论
            List<Comment> filteredUserComments = filterCommentsByLength(userComments);
            log.info("用户 {} 有 {} 条有效评论用于推荐", userId, filteredUserComments.size());

            if (filteredUserComments.isEmpty()) {
                return results;
            }

            // 3. 限制处理数量
            if (filteredUserComments.size() > MAX_COMMENTS_PER_PERSON) {
                filteredUserComments = filteredUserComments.subList(0, MAX_COMMENTS_PER_PERSON);
                log.info("限制用户评论处理数量为 {} 条", MAX_COMMENTS_PER_PERSON);
            }

            // 4. 批量处理用户评论
            processUserCommentsBatch(filteredUserComments, userId, results);

            // 5. 按匹配分数排序并去重
            List<FriendRecommendationDTO> finalResults = processAndDeduplicateResults(results);

            long endTime = System.currentTimeMillis();
            log.info("为用户 {} 生成 {} 条好友推荐，总耗时: {}ms", userId, finalResults.size(), (endTime - startTime));

            return finalResults;

        } catch (Exception e) {
            log.error("为用户 {} 推荐好友时发生错误", userId, e);
            return results;
        }
    }

    /**
     * 批量处理用户评论
     */
    private void processUserCommentsBatch(List<Comment> userComments, Long userId,
                                          List<FriendRecommendationDTO> results) {
        // 按视频分组，减少重复查询
        Map<Long, List<Comment>> commentsByVideo = userComments.stream()
                .collect(Collectors.groupingBy(Comment::getVideoId));

        int processedCount = 0;

        for (Map.Entry<Long, List<Comment>> entry : commentsByVideo.entrySet()) {
            Long videoId = entry.getKey();
            List<Comment> videoUserComments = entry.getValue();

            // 获取该视频下的所有其他用户评论（只查询一次）
            List<Comment> otherVideoComments = getOtherVideoComments(videoId, userId);

            if (otherVideoComments.isEmpty()) {
                continue;
            }

            // 批量处理该视频下的用户评论
            for (Comment userComment : videoUserComments) {
                if (processedCount >= MAX_COMMENTS_PER_PERSON) {
                    log.info("已达到最大用户评论处理限制({}条)，停止处理", MAX_COMMENTS_PER_PERSON);
                    return;
                }

                boolean success = processCommentWithTimeout(userComment, otherVideoComments, results);
                if (success) {
                    processedCount++;
                }

                if (results.size() >= MAX_RESULTS * 2) {
                    log.info("已达到推荐结果数量上限，提前结束处理");
                    return;
                }
            }
        }
    }

    /**
     * 获取视频下的其他用户评论
     */
    private List<Comment> getOtherVideoComments(Long videoId, Long userId) {
        try {
            List<Comment> videoComments = commentRepository.findByVideoIdAndUserIdNot(videoId, userId);
            List<Comment> filteredComments = filterCommentsByLength(videoComments);

            if (filteredComments.size() > MAX_COMMENTS_PER_VIDEO) {
                log.debug("视频 {} 评论数量过多({}条)，限制处理前{}条",
                        videoId, filteredComments.size(), MAX_COMMENTS_PER_VIDEO);
                return filteredComments.subList(0, MAX_COMMENTS_PER_VIDEO);
            }

            return filteredComments;
        } catch (Exception e) {
            log.error("获取视频 {} 的评论时发生错误", videoId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 带超时控制的评论处理
     */
    private boolean processCommentWithTimeout(Comment userComment, List<Comment> otherVideoComments,
                                              List<FriendRecommendationDTO> results) {
        if (otherVideoComments.isEmpty()) {
            return true;
        }

        Future<Boolean> future = executorService.submit(() -> {
            try {
                processCommentForRecommendation(userComment, otherVideoComments, results);
                return true;
            } catch (Exception e) {
                log.error("处理评论 {} 时发生错误", userComment.getCommentId(), e);
                return false;
            }
        });

        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("处理评论 {} 超时，跳过该评论", userComment.getCommentId());
            future.cancel(true);
            return false;
        } catch (Exception e) {
            log.error("处理评论 {} 时发生异常", userComment.getCommentId(), e);
            return false;
        }
    }

    /**
     * 处理单个评论的推荐逻辑
     */
    private void processCommentForRecommendation(Comment userComment, List<Comment> otherVideoComments,
                                                 List<FriendRecommendationDTO> results) {
        String userCommentContent = userComment.getContent();

        log.debug("处理用户评论 {}，对比 {} 条其他评论", userComment.getCommentId(), otherVideoComments.size());

        try {
            // 批量计算匹配分数（使用优化版）
            List<Double> matchScores = calculateMatchScoresBatch(userCommentContent, otherVideoComments);

            if (matchScores == null || matchScores.size() != otherVideoComments.size()) {
                log.warn("计算评论 {} 的匹配分数失败", userComment.getCommentId());
                return;
            }

            // 处理匹配结果
            processMatchResults(userComment, otherVideoComments, matchScores, results);

        } catch (Exception e) {
            log.error("处理评论 {} 的推荐时发生错误", userComment.getCommentId(), e);
        }
    }

    /**
     * 批量计算匹配分数（优化版）
     */
    private List<Double> calculateMatchScoresBatch(String userCommentContent, List<Comment> targetComments) {
        if (targetComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用优化版的批量匹配分计算
        try {
            List<String> commentContents = targetComments.stream()
                    .map(Comment::getContent)
                    .collect(Collectors.toList());

            return CommentSimilarityWithSentiment.INSTANCE.batchMatchScore(userCommentContent, commentContents);
        } catch (Exception e) {
            log.error("批量计算匹配分数失败", e);
            // 返回全0列表
            return targetComments.stream().map(c -> 0.0).collect(Collectors.toList());
        }
    }

    /**
     * 根据字数过滤评论
     */
    private List<Comment> filterCommentsByLength(List<Comment> comments) {
        return comments.stream()
                .filter(comment -> comment.getContent() != null && comment.getContent().length() >= MIN_COMMENT_LENGTH)
                .collect(Collectors.toList());
    }

    /**
     * 处理匹配结果
     */
    private void processMatchResults(Comment userComment, List<Comment> filteredVideoComments,
                                     List<Double> matchScores, List<FriendRecommendationDTO> results) {
        int matchCount = 0;
        for (int i = 0; i < filteredVideoComments.size(); i++) {
            double score = matchScores.get(i);

            if (score >= MATCH_SCORE_THRESHOLD) {
                Comment matchedComment = filteredVideoComments.get(i);
                Optional<FriendRecommendationDTO> resultOpt = createRecommendationDTO(userComment, matchedComment, score);

                if (resultOpt.isPresent()) {
                    synchronized (results) {
                        results.add(resultOpt.get());
                    }
                    matchCount++;
                }
            }
        }
        log.debug("找到 {} 条匹配评论", matchCount);
    }

    /**
     * 创建推荐DTO
     */
    private Optional<FriendRecommendationDTO> createRecommendationDTO(Comment userComment,
                                                                      Comment matchedComment,
                                                                      double matchScore) {
        try {
            Optional<User> userOpt = userRepository.findById(matchedComment.getUserId());
            Optional<Video> videoOpt = videoRepository.findById(matchedComment.getVideoId());

            if (userOpt.isPresent() && videoOpt.isPresent()) {
                FriendRecommendationDTO dto = new FriendRecommendationDTO(
                        userComment,
                        matchedComment,
                        videoOpt.get(),
                        userOpt.get(),
                        matchScore
                );
                return Optional.of(dto);
            }
        } catch (Exception e) {
            log.error("构建推荐DTO时发生错误，匹配评论ID: {}", matchedComment.getCommentId(), e);
        }

        return Optional.empty();
    }

    /**
     * 处理并去重结果
     */
    private List<FriendRecommendationDTO> processAndDeduplicateResults(List<FriendRecommendationDTO> results) {
        // 按匹配分数排序
        results.sort((r1, r2) -> Double.compare(r2.getMatchScore(), r1.getMatchScore()));

        // 使用Set去重，基于用户ID
        Set<Long> processedUsers = new HashSet<>();
        List<FriendRecommendationDTO> finalResults = new ArrayList<>();

        for (FriendRecommendationDTO result : results) {
            Long userId = result.getRecommendedUser().getUserId();
            if (processedUsers.add(userId) && finalResults.size() < MAX_RESULTS) {
                finalResults.add(result);
            }
        }

        return finalResults;
    }

    @Override
    public int getMinCommentLength() {
        return MIN_COMMENT_LENGTH;
    }

    @Override
    public double getMatchScoreThreshold() {
        return MATCH_SCORE_THRESHOLD;
    }
}