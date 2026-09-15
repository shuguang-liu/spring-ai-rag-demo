package com.example.service;

import com.example.config.RagProperties;
import com.example.dto.RetrieveResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
//    String template = """
//        请根据下面提供的上下文信息回答用户问题。
//
//        上下文信息：
//        ---------------------
//        {question_answer_context}
//        ---------------------
//
//        要求：
//        1. 只根据上下文回答，不要编造
//        2. 如果上下文中确实没有答案，请直接说"文档中未找到相关信息"
//        3. 回答时尽量引用上下文中的原文
//
//        用户问题：{query}
//        """;
    public ChatService(ChatModel chatModel, VectorStore vectorStore){
        // 构建带 RAG 能力的ChatClient
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
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

        // 3. 构造 Prompt
        String collect = rerank.stream().map(Document::getText).collect(Collectors.joining("\n\n --- \n\n"));


        return chatClient.prompt().user("参考资料：\n" + collect + "\n\n问题：" + question).call().content();
    }



}
