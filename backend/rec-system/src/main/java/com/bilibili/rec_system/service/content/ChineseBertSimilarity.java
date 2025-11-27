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
import java.nio.file.Paths;

public class ChineseBertSimilarity {

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;

    /**
     * 初始化BERT模型 - 使用本地模型文件
     */
    public void initialize() throws ModelNotFoundException, MalformedModelException, IOException {
        // 直接使用当前目录下的模型
        String modelName = "paraphrase-multilingual-MiniLM-L12-v2";
        File modelDir = new File(modelName);

        System.out.println("尝试加载相似度模型路径: " + modelName);

        // 列出目录内容帮助调试
        System.out.println("模型目录存在，内容:");
        File[] files = modelDir.listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println("- " + file.getName() + " (" + file.length() + " bytes)");
            }
        }

        // 检查pt文件是否存在
        File ptFile = new File(modelDir, modelName + ".pt");
        if (!ptFile.exists()) {
            System.err.println("错误：模型文件不存在: " + ptFile.getAbsolutePath());
            throw new IOException("无法找到模型PT文件");
        }

        String absolutePath = modelDir.getAbsolutePath();
        System.out.println("相似度模型路径: " + absolutePath);

        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelPath(modelDir.toPath())
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();

        try {
            model = criteria.loadModel();
            predictor = model.newPredictor();
            System.out.println("✅ 相似度模型加载成功！");
        } catch (Exception e) {
            System.err.println("加载模型时出错: " + e.getMessage());
            throw new IOException("无法加载模型: " + e.getMessage(), e);
        }
    }

    /**
     * 获取句子的向量表示
     */
    public float[] getEmbedding(String text) throws TranslateException {
        return predictor.predict(text);
    }

    /**
     * 计算余弦相似度
     */
    public double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算两个句子的相似度
     */
    public double calculateSimilarity(String sentence1, String sentence2)
            throws TranslateException {
        float[] embedding1 = getEmbedding(sentence1);
        float[] embedding2 = getEmbedding(sentence2);
        return cosineSimilarity(embedding1, embedding2);
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
    }

    public static void main(String[] args) {

        ChineseBertSimilarity bert = new ChineseBertSimilarity();

        try {
            System.out.println("初始化相似度模型...");
            bert.initialize();

            String s1 = "看完这个视频真的又一次感叹詹姆斯的伟大！快 40 岁了还能保持这种状态，简直是人类体能天花板。无论是突破、分球还是比赛阅读能力，老詹依然是联盟顶级。很多人只看数据却忽略了他对球队体系的影响，只要他在场上，队友的整体表现就是不一样。真的希望大家多看看他在细节上的处理，而不是只用年龄黑来否定这位历史级别的球员。";
            String s2 = "说实话，这场比赛詹姆斯的发挥就是老将教科书级别的存在。他在关键时刻的几次选择非常成熟，不管是用身体吃对抗杀进内线，还是吸引包夹后精准分球，都体现了他对比赛的掌控。虽然现在运动能力不像巅峰，但他靠经验、判断和节奏完全弥补了这一点。能在这个年龄依旧影响比赛走势，这就是为什么我认为詹姆斯是史上最全面球员之一。";
            //String s3 = "今天下雨了，我出门买菜很不方便";

            double sim12 = bert.calculateSimilarity(s1, s2);

            System.out.println("\n=== 测试结果 ===");
            System.out.printf("句子1: %s\n句子2: %s\n相似度: %.4f\n\n", s1, s2, sim12);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bert.close();
        }
    }

}
