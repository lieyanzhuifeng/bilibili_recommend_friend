package com.bilibili.rec_system.service.content;

import ai.djl.translate.TranslateException;
import lombok.Getter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum CommentSimilarityWithSentiment {
    INSTANCE;

    private final ChineseBertSimilarity bertSim = new ChineseBertSimilarity();
    private final ChineseSentimentClassifier sentiment = new ChineseSentimentClassifier();

    // 统一使用 ConcurrentHashMap，确保多线程下 getCachedSentiment 安全
    private final Map<String, ChineseSentimentClassifier.SentimentResult> sentimentCache = new ConcurrentHashMap<>();

    @Getter private volatile boolean initialized = false;
    private final Object initLock = new Object();

    private static final double EARLY_TERMINATION_THRESHOLD = 0.7;
    private static final double PENALTY_WEIGHT = 0.2;

    public void init() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    try {
                        bertSim.initialize();
                        sentiment.initialize();
                        initialized = true;
                        System.out.println("✅ CommentSimilarityWithSentiment 批量优化版初始化成功");
                    } catch (Exception e) {
                        System.err.println("初始化失败: " + e.getMessage());
                        initialized = false;
                    }
                }
            }
        }
    }

    /**
     * 批量计算匹配分数 (核心性能提升方法)
     */
    public List<Double> batchMatchScore(String userComment, List<String> commentList) throws Exception {
        if (!initialized || commentList.isEmpty()) return Collections.nCopies(commentList.size(), 0.0);

        // 1. BERT 批量推理 (一次推理计算所有评论向量)
        List<Double> baseSims = bertSim.calculateSimilaritiesBatch(userComment, commentList);

        // 2. 预获取用户评论情感
        ChineseSentimentClassifier.SentimentResult userSentiment = getCachedSentiment(userComment);

        // 3. 并行计算情感惩罚 (情感分析为 CPU 密集型，使用 parallelStream 加速)
        return IntStream.range(0, commentList.size())
                .parallel()
                .mapToObj(i -> {
                    double baseSim = baseSims.get(i);
                    // 提前终止逻辑：相似度低于门槛不进行昂贵的情感分析
                    if (baseSim < EARLY_TERMINATION_THRESHOLD) return 0.0;

                    try {
                        String targetText = commentList.get(i);
                        ChineseSentimentClassifier.SentimentResult targetSent = getCachedSentiment(targetText);
                        double penalty = calculateSentimentPenalty(userSentiment, targetSent);
                        return Math.max(0, baseSim - penalty);
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .collect(Collectors.toList());
    }

    private ChineseSentimentClassifier.SentimentResult getCachedSentiment(String text) {
        return sentimentCache.computeIfAbsent(text, k -> {
            try { return sentiment.predictEnhanced(k); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private double calculateSentimentPenalty(ChineseSentimentClassifier.SentimentResult r1,
                                             ChineseSentimentClassifier.SentimentResult r2) {
        return PENALTY_WEIGHT * (Math.abs(r1.getPositiveScore() - r2.getPositiveScore())
                + Math.abs(r1.getNegativeScore() - r2.getNegativeScore()));
    }

    public double similarity(String s1, String s2) throws TranslateException {
        try {
            List<Double> res = batchMatchScore(s1, List.of(s2));
            return res.get(0);
        } catch (Exception e) { return 0.0; }
    }

    public void close() {
        if (initialized) {
            bertSim.close();
            sentiment.close();
            sentimentCache.clear();
            initialized = false;
        }
    }

    public double getEarlyTerminationThreshold() { return EARLY_TERMINATION_THRESHOLD; }
}