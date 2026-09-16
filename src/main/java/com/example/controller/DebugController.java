package com.example.controller;

import com.example.service.RerankService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liushug
 * @description TODO
 */
@RequiredArgsConstructor
@RestController
@Profile("dev")
public class DebugController {

    private final VectorStore vectorStore;

    private final RerankService rerankService;



    @GetMapping("/debug-search")
    public List<Map<String, Object>> debugSearch(@RequestParam String question) {
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(0.0)   // 先设为 0，把所有结果都捞出来看
                .build();

        return vectorStore.similaritySearch(request).stream()
                .map(doc -> Map.of(
                        "content", doc.getText(),
                        "metadata", doc.getMetadata(),
                        "score", doc.getMetadata().getOrDefault("distance", "N/A")
                ))
                .toList();
    }

    @GetMapping("/debug-rerank")
    public Map<String, Object> debugrerank(@RequestParam String question) {
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(0.0)   // 先设为 0，把所有结果都捞出来看
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        // 重排前
        List<Map<String, Object>> list = documents.stream()
                .map(doc -> Map.of(
//                        "content", doc.getText(),
                        "metadata", doc.getMetadata(),
                        "score", doc.getMetadata().getOrDefault("distance", "N/A")
                ))
                .toList();

        // 重排后
        ResponseEntity<Map> rerank = rerankService.rerank(question, documents);
        // 3. Rerank 后：解析返回的 index + relevance_score
        List<Map<String, Object>> afterRerank = new ArrayList<>();
        if (rerank.getBody() != null && rerank.getBody().containsKey("output")) {
            Map<String, Object> output = (Map<String, Object>) rerank.getBody().get("output");
            List<Map<String, Object>> results = (List<Map<String, Object>>) output.get("results");
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    Map<String, Object> result = results.get(i);
                    int index = (Integer) result.get("index");
                    double score = (Double) result.get("relevance_score");
//                    Document originalDoc = documents.get(index);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("rank", i + 1);
//                    item.put("content", originalDoc.getText());
                    item.put("rerankScore", score);

                    // 计算排名变化
                    int originalRank = index + 1;
                    if (i + 1 < originalRank) {
                        item.put("change", String.format("↑ 从第%d升至第%d", originalRank, i + 1));
                    } else if (i + 1 > originalRank) {
                        item.put("change", String.format("↓ 从第%d降至第%d", originalRank, i + 1));
                    }
                    afterRerank.add(item);
                }

            }

        }
        // 4. 组装最终返回结果
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("query", question);
        finalResponse.put("before_rerank", list);
        finalResponse.put("after_rerank", afterRerank);
        return finalResponse;
    }

}
