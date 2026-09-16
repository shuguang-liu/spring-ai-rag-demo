package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liushug
 * @description 重排配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Retrieve retrieve = new Retrieve();
    private Rerank rerank = new Rerank();

    // 嵌套类：向量检索配置
    @Data
    public static class Retrieve {
        private int topK = 10;
        private double similarityThreshold = 0.3;
    }

    // 嵌套类：Rerank 重排配置
    @Data
    public static class Rerank {
        private boolean enabled = true;
        private int topN = 5;
        private String model = "qwen3.7-text-rerank";
    }

}
