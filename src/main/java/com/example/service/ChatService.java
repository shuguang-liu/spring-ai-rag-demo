package com.example.service;

import com.example.config.RagProperties;
import com.example.dto.RetrieveResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liusg
 * @version 1.0
 */
@Service
public class ChatService {

    private final ChatClient chatClient;

    @Autowired
    private RerankService rerankService;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RagProperties ragProperties;

    //
//    // 自定义中文 RAG Prompt 模板
    String template = """
            请根据下面的参考资料回答用户问题。
            
             === 参考资料开始 ===
             {question_answer_context}
             === 参考资料结束 ===
            
             ### 用户问题 ###
             {query}
             ### 用户问题结束 ###
            
             只使用参考资料中的信息回答。如果资料中没有答案，直接回复"知识库中未找到相关信息"。
        """;
    public ChatService(ChatModel chatModel, VectorStore vectorStore){
        // 构建带 RAG 能力的ChatClient
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .promptTemplate(new PromptTemplate(template))
                                .searchRequest(SearchRequest.builder()
                                        .topK(20)
                                        .similarityThreshold(0.3)
                                        .build())
                                .build()
                ).build();
    }

    public String ordinaryAsk(String question) {
        return chatClient.prompt().user(question).call().content();
    }

    public String ask(String question) {
        // 1. 粗召回
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(20).similarityThreshold(0.3).build());

        // 2. 精排
        List<Document> rerank = rerankService.rerank(question, documents, ragProperties.getRerank().getTopN());
        // 打印第一条看看结构
        System.out.println("=== Rerank 原始结果 ===");
        System.out.println(rerank.get(0));
        // 补充：3. 动态阈值判断
        if (rerank.isEmpty()) {
            return "知识库中未找到相关信息";
        }
        // 补充：4. 绝对阈值：最高分 < 0.3 → 直接拒答
        double topScore = getTopScore(rerank);
        System.out.println("=== Rerank 最高分: " + topScore);
        if (topScore < 0.3) {
            System.out.println("=== 低于阈值，直接拒答");
            return "知识库中未找到相关信息";
        }

        // 补充：5. 相对断崖截断
        List<Document> cutDocs = cutoffByCliff(rerank);
        System.out.println("=== 断崖截断后: " + cutDocs.size() + " 条");


        // 6. 构造 Prompt
        String collect = rerank.stream().map(Document::getText).collect(Collectors.joining("\n\n --- \n\n"));


        return chatClient.prompt().user("参考资料：\n" + collect + "\n\n问题：" + question).call().content();
    }

    private List<Document> cutoffByCliff(List<Document> docs) {
        if (docs.size() <= 2) return docs;
        List<Document> result = new ArrayList<>();
        result.add(docs.get(0));
        for (int i = 1; i < docs.size(); i++) {
            double prevScore = getScore(docs.get(i-1));
            double currScore = getScore(docs.get(i));
            // 分数掉超过 50% → 断崖，停止
            if (currScore < prevScore * 0.5) break;
            result.add(docs.get(i));
        }
        return result;
    }

    private double getScore(Document doc) {
        return doc.getScore() == null ? 0.0 : doc.getScore();
    }

    private double getTopScore(List<Document> docs) {
        return docs.isEmpty() ? 0.0 : getScore(docs.get(0));
    }



}
