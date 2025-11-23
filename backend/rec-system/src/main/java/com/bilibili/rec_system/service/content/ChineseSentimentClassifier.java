package com.bilibili.rec_system.service.content;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextClassificationTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 精简版中文情感分析器
 * ⭐ 去掉星级计算，只保留情感向量（positive, neutral, negative）
 */
public class ChineseSentimentClassifier {

    private ZooModel<String, Classifications> model;
    private Predictor<String, Classifications> predictor;

    // 特征词库
    private static final String[] VIDEO_NEGATIVE_WORDS = {
            "恰饭",
            "标题党","骗点击","封面党",
            "抄袭","盗视频","剽窃",
            "取关","脱粉","拉黑","屏蔽","取消关注",
            "水视频","凑数","糊弄","敷衍",
            "江郎才尽","走下坡路","不如以前","质量下降",
            "浪费时间","浪费流量","白看了"
    };

    private static final String[] CLEAR_POSITIVE_WORDS = {
            "绝了","神作","封神","天花板","巅峰","完美",
            "惊艳","震撼","顶级","一流","超神"
    };

    /** 初始化模型 */
    public void initialize() throws ModelNotFoundException, MalformedModelException, IOException {
        File modelDir = new File("bert-base-multilingual-uncased-sentiment");
        if (!modelDir.exists()) {
            throw new IOException("情感分析模型目录不存在: " + modelDir.getAbsolutePath());
        }

        Criteria<String, Classifications> criteria = Criteria.builder()
                .setTypes(String.class, Classifications.class)
                .optModelPath(modelDir.toPath())
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextClassificationTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();

        model = criteria.loadModel();
        predictor = model.newPredictor();
        System.out.println("✅ 情感分析模型加载成功！");
    }

    /** 增强预测 - 返回情感向量 */
    public SentimentResult predictEnhanced(String text) throws TranslateException {
        Classifications classifications = predictor.predict(text);

        double positiveScore = 0.0;
        double negativeScore = 0.0;
        double neutralScore = 0.0;

        List<String> classNames = classifications.getClassNames();
        List<Double> probs = classifications.getProbabilities();

        for (int i = 0; i < classNames.size(); i++) {
            double p = probs.get(i);
            // 直接根据模型类别分配向量，不再计算星级
            if (classNames.get(i).contains("4") || classNames.get(i).contains("5")) positiveScore += p;
            else if (classNames.get(i).contains("1") || classNames.get(i).contains("2")) negativeScore += p;
            else neutralScore += p;
        }

        // 特征词加权
        for (String word : VIDEO_NEGATIVE_WORDS) if (text.contains(word)) negativeScore += 0.1;
        for (String word : CLEAR_POSITIVE_WORDS) if (text.contains(word)) positiveScore += 0.1;

        // 归一化
        double total = positiveScore + negativeScore + neutralScore;
        positiveScore /= total;
        negativeScore /= total;
        neutralScore /= total;

        // 最终情感判断
        String finalSentiment;
        if (positiveScore >= negativeScore && positiveScore >= neutralScore) finalSentiment = "积极";
        else if (negativeScore >= positiveScore && negativeScore >= neutralScore) finalSentiment = "消极";
        else finalSentiment = "中性";

        boolean corrected = false;
        for (String word : VIDEO_NEGATIVE_WORDS) if (text.contains(word)) corrected = true;
        for (String word : CLEAR_POSITIVE_WORDS) if (text.contains(word)) corrected = true;

        return new SentimentResult(text, finalSentiment, positiveScore, neutralScore, negativeScore, corrected);
    }

    /** 获取情感向量，方便余弦计算 */
    public double[] getSentimentVector(SentimentResult r) {
        return new double[]{r.getPositiveScore(), r.getNeutralScore(), r.getNegativeScore()};
    }

    /** 关闭资源 */
    public void close() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }

    /** 情感结果类 */
    public static class SentimentResult {
        private String text;
        private String sentiment;      // 积极, 消极, 中性
        private double positiveScore;
        private double neutralScore;
        private double negativeScore;
        private boolean corrected;

        public SentimentResult(String text, String sentiment,
                               double positiveScore, double neutralScore,
                               double negativeScore, boolean corrected) {
            this.text = text;
            this.sentiment = sentiment;
            this.positiveScore = positiveScore;
            this.neutralScore = neutralScore;
            this.negativeScore = negativeScore;
            this.corrected = corrected;
        }

        public String getText() { return text; }
        public String getSentiment() { return sentiment; }
        public double getPositiveScore() { return positiveScore; }
        public double getNeutralScore() { return neutralScore; }
        public double getNegativeScore() { return negativeScore; }
        public boolean isCorrected() { return corrected; }
    }
}
