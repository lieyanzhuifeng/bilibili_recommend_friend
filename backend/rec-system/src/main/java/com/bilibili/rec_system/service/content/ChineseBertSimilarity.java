package com.bilibili.rec_system.service.content;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChineseBertSimilarity {

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;

    /**
     * 初始化BERT模型 - 适配 0.28.0 批量推理
     */
    public void initialize() throws ModelNotFoundException, MalformedModelException, IOException {
        String modelName = "paraphrase-multilingual-MiniLM-L12-v2";
        File modelDir = new File(modelName);

        if (!modelDir.exists()) {
            throw new IOException("模型目录不存在: " + modelDir.getAbsolutePath());
        }

        // 定义为单条 String -> float[]，由 batchPredict 自动处理攒批
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelPath(modelDir.toPath())
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optArgument("pooling", "mean") // 显式指定 Mean Pooling
                .optProgress(new ProgressBar())
                .build();

        try {
            model = criteria.loadModel();
            predictor = model.newPredictor();
            System.out.println("✅ BERT 批量推理模型初始化成功！");
        } catch (Exception e) {
            throw new IOException("无法加载模型: " + e.getMessage(), e);
        }
    }

    /**
     * 核心加速方法：批量获取向量
     */
    public List<float[]> getEmbeddingsBatch(List<String> texts) throws TranslateException {
        if (texts == null || texts.isEmpty()) return new ArrayList<>();
        // 使用 0.28.0 推荐的 batchPredict，内部自动并行优化
        return predictor.batchPredict(texts);
    }

    /**
     * 一对多批量计算相似度
     */
    public List<Double> calculateSimilaritiesBatch(String source, List<String> targets) throws TranslateException {
        if (targets == null || targets.isEmpty()) return new ArrayList<>();

        List<String> allTexts = new ArrayList<>();
        allTexts.add(source);
        allTexts.addAll(targets);

        // N 条数据只触发 1 次推理请求
        List<float[]> embeddings = getEmbeddingsBatch(allTexts);
        float[] sourceVec = embeddings.get(0);

        List<Double> results = new ArrayList<>(targets.size());
        for (int i = 1; i < embeddings.size(); i++) {
            results.add(cosineSimilarity(sourceVec, embeddings.get(i)));
        }
        return results;
    }

    public double cosineSimilarity(float[] vec1, float[] vec2) {
        double dotProduct = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        return (norm1 == 0 || norm2 == 0) ? 0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public double calculateSimilarity(String s1, String s2) throws TranslateException {
        List<Double> res = calculateSimilaritiesBatch(s1, List.of(s2));
        return res.isEmpty() ? 0.0 : res.get(0);
    }

    public void close() {
        if (predictor != null) predictor.close();
        if (model != null) model.close();
    }
    public static void main(String[] args) throws Exception {
        ChineseBertSimilarity service = new ChineseBertSimilarity();
        service.initialize();

        String source = "我非常喜欢这顿午饭，味道很好。";
        List<String> targets = List.of(
                "这顿午餐的味道真是不错，我很满意。",
                "今天的天气挺好的，适合出去玩。",
                "虽然有点贵，但午饭的口感确实一流。"
        );

        long start = System.currentTimeMillis();
        List<Double> results = service.calculateSimilaritiesBatch(source, targets);
        long end = System.currentTimeMillis();

        System.out.println("批量推理耗时: " + (end - start) + "ms");
        results.forEach(score -> System.out.println("相似度分值: " + score));

        service.close();
    }

}