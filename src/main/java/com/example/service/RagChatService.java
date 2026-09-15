package com.example.service;

import com.example.dto.RetrieveResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liushug
 * @date 2026/8/13 11:00
 * @description RAG 问答服务
 */
@Service
public class RagChatService {

    private final ChatClient chatClient;

    public RagChatService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是一个专业的Java技术助手。请严格基于提供的参考资料回答用户问题。
                        
                                                规则：
                                                1. 只使用参考资料中的信息回答，不要编造
                                                2. 如果参考资料中没有相关信息，明确告知用户"知识库中未找到相关信息"
                                                3. 回答要简洁专业，适当给出代码示例
                                                4. 在回答末尾标注信息来源
                        """)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(5) // 搜索最近的 5 个文档
                                .similarityThreshold(0.5)  // 相似度阈值，大于该阈值才返回
                                .build())
                        .build())
                .build();
    }


    /**
     * RAG 问答
     * @param question 用户问题
     * @return 基于知识库回答
     */
    public String ask(String question){
        return chatClient.prompt().user(question).call().content();
    }




}
