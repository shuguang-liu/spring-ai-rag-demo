package com.example.service;

import com.example.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author liusg
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class RerankService {

    private final String RERANK_URL = "https://ws-mr2meeupuehmjzzn.cn-beijing.maas.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank";

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RagProperties ragProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Document> rerank(String query, List<Document> candidates, int topN) {
        // 1. 构造请求体
        Map<String, Object> body = Map.of("model", ragProperties.getRerank().getModel(),
                "input", Map.of(
                        "query", query,
                        "documents", candidates.stream().map(Document::getText).toList()
                ),
                "parameters", Map.of("top_n", topN, "return_documents", ragProperties.getRerank().isEnabled()));
        // 2. 请求头带API KEY
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 3. 调用
        ResponseEntity<Map> resp = restTemplate.exchange(
                RERANK_URL, HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class
        );
        // 4. 解析返回的 index + relevance_score，重排原 candidates
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) ((Map) resp.getBody().get("output")).get("results");

        return results.stream()
                .map(r -> candidates.get((Integer) r.get("index")))
                .toList();

    }

    public ResponseEntity<Map> rerank(String query, List<Document> candidates) {
        // 1. 构造请求体
        Map<String, Object> body = Map.of("model", ragProperties.getRerank().getModel(),
                "input", Map.of(
                        "query", query,
                        "documents", candidates.stream().map(Document::getText).toList()
                ),
                "parameters", Map.of("top_n", ragProperties.getRerank().getTopN(), "return_documents", ragProperties.getRerank().isEnabled()));
        // 2. 请求头带API KEY
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 3. 调用
        ResponseEntity<Map> resp = restTemplate.exchange(
                RERANK_URL, HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class
        );


        return resp;

    }


}
