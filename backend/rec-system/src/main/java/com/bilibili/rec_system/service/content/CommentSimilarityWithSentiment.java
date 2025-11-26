package com.bilibili.rec_system.service.content;

import ai.djl.translate.TranslateException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 ChineseBertSimilarity + ChineseSentimentClassifier 的
 * 文本相似度（句向量） + 情感惩罚综合评分模块
 * ⭐ 优化版：句向量相似度低时直接返回0，跳过情感分析
 */
public enum CommentSimilarityWithSentiment {
    INSTANCE;

    private final ChineseBertSimilarity bertSim;
    private final ChineseSentimentClassifier sentiment;

    @Getter
    private volatile boolean initialized = false;
    private final Object initLock = new Object();

    // 情感分析结果缓存
    private final Map<String, ChineseSentimentClassifier.SentimentResult> sentimentCache;
    private static final int MAX_CACHE_SIZE = 10000; // 增大缓存容量

    // 优化参数
    private static final double EARLY_TERMINATION_THRESHOLD = 0.5; // 提前终止阈值
    private static final double PENALTY_WEIGHT = 0.2; // 情感惩罚权重

    CommentSimilarityWithSentiment() {
        this.bertSim = new ChineseBertSimilarity();
        this.sentiment = new ChineseSentimentClassifier();
        this.sentimentCache = new LinkedHashMap<String, ChineseSentimentClassifier.SentimentResult>(
                MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ChineseSentimentClassifier.SentimentResult> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }

    /**
     * 初始化方法，加载模型
     */
    public void init() {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    System.out.println("正在初始化 CommentSimilarityWithSentiment 模型...");
                    long startTime = System.currentTimeMillis();

                    try {
                        bertSim.initialize();
                        sentiment.initialize();
                        initialized = true;
                        long endTime = System.currentTimeMillis();
                        System.out.printf("CommentSimilarityWithSentiment 初始化完成，耗时: %dms\n", (endTime - startTime));
                    } catch (Exception e) {
                        System.err.println("CommentSimilarityWithSentiment 模型初始化失败: " + e.getMessage());
                        e.printStackTrace();
                        // 模型加载失败不抛出异常，允许服务继续运行
                        initialized = false;
                    }
                }
            }
        }
    }

    /**
     * 确保已初始化的方法
     */
    private boolean ensureInitialized() {
        if (!initialized) {
            System.err.println("CommentSimilarityWithSentiment 未初始化，部分功能可能不可用");
            return false;
        }
        return true;
    }

    /**
     * 带缓存的情感分析
     */
    private ChineseSentimentClassifier.SentimentResult getCachedSentiment(String text) {
        synchronized (sentimentCache) {
            return sentimentCache.computeIfAbsent(text, k -> {
                try {
                    return sentiment.predictEnhanced(k);
                } catch (Exception e) {
                    throw new RuntimeException("情感分析失败: " + k, e);
                }
            });
        }
    }

    /**
     * 计算情感惩罚值
     */
    private double calculateSentimentPenalty(ChineseSentimentClassifier.SentimentResult r1,
                                             ChineseSentimentClassifier.SentimentResult r2) {
        double pos1 = r1.getPositiveScore();
        double neg1 = r1.getNegativeScore();
        double pos2 = r2.getPositiveScore();
        double neg2 = r2.getNegativeScore();

        double penalty = PENALTY_WEIGHT * (Math.abs(pos1 - pos2) + Math.abs(neg1 - neg2));
        return Math.min(penalty, 1.0);
    }

    /**
     * ⭐ 优化版综合相似度：句向量相似度低时直接返回0，跳过情感分析
     */
    public double similarity(String s1, String s2) throws TranslateException {
        if (!ensureInitialized()) {
            return 0.0; // 未初始化时返回默认相似度0
        }

        // 1. 先计算句向量相似度
        double baseSim = bertSim.calculateSimilarity(s1, s2);

        // 2. 提前终止检查：句向量相似度低时直接返回0
        if (baseSim < EARLY_TERMINATION_THRESHOLD) {
            System.out.printf(
                    "\n===== 提前终止分析 =====\n" +
                            "句向量相似度: %.4f < 阈值 %.2f\n" +
                            "直接返回相似度: 0.0\n",
                    baseSim, EARLY_TERMINATION_THRESHOLD
            );
            return 0.0;
        }

        // 3. 只有句向量相似度足够高时，才进行情感分析
        ChineseSentimentClassifier.SentimentResult r1 = getCachedSentiment(s1);
        ChineseSentimentClassifier.SentimentResult r2 = getCachedSentiment(s2);

        double penalty = calculateSentimentPenalty(r1, r2);
        double finalScore = Math.max(0, Math.min(1, baseSim - penalty));

        System.out.printf(
                "\n===== 综合相似度分析 =====\n" +
                        "句向量相似度: %.4f\n" +
                        "情感向量1: [%.3f, %.3f]\n" +
                        "情感向量2: [%.3f, %.3f]\n" +
                        "情感惩罚: %.4f\n" +
                        "最终相似度: %.4f\n",
                baseSim,
                r1.getPositiveScore(), r1.getNegativeScore(),
                r2.getPositiveScore(), r2.getNegativeScore(),
                penalty,
                finalScore
        );

        return finalScore;
    }

    /**
     * ⭐ 优化版批量匹配分
     */
    public List<Double> batchMatchScore(String comment, List<String> commentList) throws Exception {
        if (!ensureInitialized()) {
            // 未初始化时返回全0的相似度列表
            List<Double> defaultScores = new ArrayList<>(commentList.size());
            for (int i = 0; i < commentList.size(); i++) {
                defaultScores.add(0.0);
            }
            return defaultScores;
        }

        // 预计算用户评论的情感（因为可能用到）
        ChineseSentimentClassifier.SentimentResult userSentiment = getCachedSentiment(comment);

        List<Double> scores = new ArrayList<>();
        int earlyTerminationCount = 0;
        int fullCalculationCount = 0;

        for (String candidate : commentList) {
            // 先计算基础相似度
            double baseSim = bertSim.calculateSimilarity(comment, candidate);

            if (baseSim < EARLY_TERMINATION_THRESHOLD) {
                scores.add(0.0);
                earlyTerminationCount++;
            } else {
                // 只有基础相似度足够高时，才计算情感惩罚
                ChineseSentimentClassifier.SentimentResult candidateSentiment = getCachedSentiment(candidate);
                double penalty = calculateSentimentPenalty(userSentiment, candidateSentiment);
                double finalScore = Math.max(0, baseSim - penalty);
                scores.add(finalScore);
                fullCalculationCount++;
            }
        }

        System.out.printf("批量匹配统计: 提前终止=%d, 完整计算=%d, 总计=%d\n",
                earlyTerminationCount, fullCalculationCount, commentList.size());

        return scores;
    }

    /**
     * 工具方法：一次性关闭所有模型
     */
    public void close() {
        if (initialized) {
            synchronized (initLock) {
                if (initialized) {
                    System.out.println("正在关闭 CommentSimilarityWithSentiment 模型...");
                    bertSim.close();
                    sentiment.close();
                    clearCache();
                    initialized = false;
                    System.out.println("CommentSimilarityWithSentiment 模型已关闭");
                }
            }
        }
    }

    /**
     * 重新初始化方法
     */
    public void reinit() {
        synchronized (initLock) {
            if (initialized) {
                close();
            }
            init(); // init() 现在不抛出异常
        }
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        synchronized (sentimentCache) {
            sentimentCache.clear();
            System.out.println("情感分析缓存已清空");
        }
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        synchronized (sentimentCache) {
            return sentimentCache.size();
        }
    }

    /**
     * 获取提前终止阈值
     */
    public double getEarlyTerminationThreshold() {
        return EARLY_TERMINATION_THRESHOLD;
    }

    /**
     * ===== 测试入口 =====
     */
    public static void main(String[] args) throws Exception {
        CommentSimilarityWithSentiment.INSTANCE.init(); // 现在不抛出异常

        String userComment = "说实话，这场比赛詹姆斯的发挥就是老将教科书级别的存在。他在关键时刻的几次选择非常成熟，不管是用身体吃对抗杀进内线，还是吸引包夹后精准分球，都体现了他对比赛的掌控。虽然现在运动能力不像巅峰，但他靠经验、判断和节奏完全弥补了这一点。能在这个年龄依旧影响比赛走势，这就是为什么我认为詹姆斯是史上最全面球员之一。";

        List<String> commentList = List.of(
                "看完这个视频真的又一次感叹詹姆斯的伟大！快 40 岁了还能保持这种状态，简直是人类体能天花板。无论是突破、分球还是比赛阅读能力，老詹依然是联盟顶级。很多人只看数据却忽略了他对球队体系的影响，只要他在场上，队友的整体表现就是不一样。真的希望大家多看看他在细节上的处理，而不是只用年龄黑来否定这位历史级别的球员。",
                "库里的三分球太魔幻了！视频里的几个投篮动作都堪称艺术，每次出手都让对手绝望。他改变了整个联盟的打法，真是现代篮球的革命者",
                "科比的精神永远是我的榜样。虽然已经退役，但每次回看比赛都能感受到他的斗志和杀手本能，视频里他最后一投，完美演绎传奇。",
                "老詹打球越来越慢了，队伍老化明显，每次关键时刻都是他拖累球队。大家别被数据骗了，真正能决定比赛的年轻球员才是核心，老詹只是花瓶，没有什么实际的作用。",
                "又是一场老詹表演秀，但结果还是输了。他靠身体吃对抗确实强，可一旦对手用战术限制他，整队就没办法运转。年龄和退化是无法回避的事实。"

        );

        // 测试单个相似度计算
        System.out.println("=== 测试单个相似度计算 ===");
        for (String candidate : commentList) {
            double score = INSTANCE.similarity(userComment, candidate);
            System.out.printf("与候选评论的相似度: %.4f\n\n", score);
        }

        // 测试批量匹配
        System.out.println("=== 测试批量匹配 ===");
        List<Double> scores = INSTANCE.batchMatchScore(userComment, commentList);
        for (int i = 0; i < commentList.size(); i++) {
            System.out.printf("候选评论 [%d] 匹配分: %.4f\n", i + 1, scores.get(i));
        }

        INSTANCE.close();
    }
}