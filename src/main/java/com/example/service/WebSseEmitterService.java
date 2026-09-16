package com.example.service;

import com.example.config.RagProperties;
import com.example.config.SseState;
import com.example.dto.EvalResult;
import com.example.dto.RetrieveResult;
import com.example.dto.SseEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author liushug
 * @description
 */
@Slf4j
@Service
public class WebSseEmitterService {

//    private final String SYSTEM_PROMPT = """
//                                                请严格基于提供的参考资料回答用户问题。
//                                                规则：
//                                                1. 只使用参考资料中的信息回答，不要编造
//                                                2. 如果参考资料中没有相关信息，明确告知用户"知识库中未找到相关信息"
//                                                3. 只输出纯文本格式
//                                                4. 在回答末尾标注信息来源
//                        """;
    private final String SYSTEM_PROMPT = """
            你是一个专业的Java技术助手，负责基于知识库回答用户问题。
    
            规则：
            1. 参考资料通过 === 分隔符包裹，只使用其中的信息回答
            2. 用户问题通过 ### 分隔符标注，位于参考资料之后
            3. 只使用参考资料中的信息回答，不要编造
            4. 如果参考资料中没有相关信息，明确告知用户"知识库中未找到相关信息"
            5. 只输出纯文本格式
            6. 在回答末尾标注信息来源
            """;

    private final ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private RagProperties ragProperties;

    @Autowired
    private RerankService rerankService;

    public WebSseEmitterService(ChatModel chatModel, VectorStore vectorStore){
        this.chatClient = ChatClient.builder(chatModel).defaultAdvisors(
                QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder().topK(20)
                                .similarityThreshold(0.3).build()).build()
        ).build();
    }




    public void askStreamWithEmitter(String question, SseEmitter sseEmitter) {
        // 用独立线程执行，不能占用请求线程
        CompletableFuture.runAsync(() -> {
            try{
                // 检索
                sseEmitter.send(SseEmitter.event().data(SseEvent.state(SseState.RETRIEVING)));
                RetrieveResult retrieveResult = retrieveAndRerank(question);

                // 重排
                sseEmitter.send(SseEmitter.event().data(SseEvent.state(SseState.RETRIEVING)));
                sseEmitter.send(SseEmitter.event().data(SseEvent.think("召回" + retrieveResult.getCandidates().size()+ "条数据，Rerank 后保留" + retrieveResult.getTopDocs().size() + "条")));

                // 生成
                sseEmitter.send(SseEmitter.event().data(SseEvent.state(SseState.GENERATING)));
                String s = buildContext(retrieveResult.getTopDocs());
                // 流式生成
                chatClient.prompt().system(SYSTEM_PROMPT).user("参考资料：\n" + s + "\n\n问题：" + question)
                        .stream().content().toIterable()
                        .forEach(chunk -> {
                            try{
                                sseEmitter.send(SseEmitter.event().data(SseEvent.message(chunk)));
                            } catch (IOException e){
                                throw new RuntimeException("SSE send failed", e);
                            }
                        });
                sseEmitter.send(SseEmitter.event().data(SseEvent.state(SseState.DONE)));
                sseEmitter.complete();

            }catch (Exception e){
                try {
                    sseEmitter.send(SseEmitter.event().data(SseState.ERROR));
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            }
        });
    }

    public EvalResult evalOne(String question, String groundTruth){
        log.info("===收到问题：[{}]", question);
        // 复用现有链路
        RetrieveResult retrieveResult = retrieveAndRerank(question);
        List<String> contexts = retrieveResult.getTopDocs().stream().map(Document::getText).toList();
        // 拼接处改造
        String userMessage = """
        === 参考资料开始 ===
        %s
        === 参考资料结束 ===

        ### 用户问题开始 ###
        %s
        ### 用户问题结束 ###

        请回答上面【用户问题】中的提问。
        """.formatted(buildContext(retrieveResult.getTopDocs()), question);
        String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(userMessage).call().content();

        return new EvalResult(question, groundTruth, answer, contexts);
    }


    /**
     * 带向量搜索的问答 流式问答工具
     * @param question
     * @return
     */
    private RetrieveResult retrieveAndRerank(String question){
        List<Document> candidates = vectorStore.similaritySearch(SearchRequest.builder().query(question)
                .topK(ragProperties.getRetrieve().getTopK()).similarityThreshold(ragProperties.getRetrieve().getSimilarityThreshold())
                .build());
        List<Document> topDocs = rerankService.rerank(question, candidates, ragProperties.getRerank().getTopN());
        return new RetrieveResult(candidates, topDocs);
    }

    private String buildContext(List<Document> docs) {
        String ctx = docs.stream().map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
        // 超过 6000 字截断
        if (ctx.length() > 6000) {
            ctx = ctx.substring(0, 6000) + "\n\n...(内容过长已截断)";
        }
        return ctx;
    }

    public Flux<ServerSentEvent<SseEvent>> askStreamFlux(String question) {
        return Flux.create(sink -> {
            try{


            // 检索
            sink.next(sse(SseEvent.state(SseState.RETRIEVING)));
            RetrieveResult retrieveResult = retrieveAndRerank(question);

            // 重排
            sink.next(sse(SseEvent.state(SseState.RETRIEVING)));
            sink.next(sse(SseEvent.think("召回" + retrieveResult.getCandidates().size()+ "条数据，Rerank 后保留" + retrieveResult.getTopDocs().size() + "条")));
            sink.next(sse(SseEvent.state(SseState.GENERATING)));
            String context = buildContext(retrieveResult.getTopDocs());
            chatClient.prompt().system(SYSTEM_PROMPT).user("参考资料：\n" + context + "\n\n问题：" + question).stream().content()
                    .subscribe(chunk -> sink.next(sse(SseEvent.message(chunk))),
                            error -> {
                                sink.next(sse(SseEvent.state(SseState.ERROR)));
                                sink.error(error);
                            },
                            () -> {
                                sink.next(sse(SseEvent.state(SseState.DONE)));
                                sink.complete();
                            });

        } catch (Exception e){
                sink.next(sse(SseEvent.state(SseState.ERROR)));
                sink.error(e);
            }
        });
    }

    private ServerSentEvent<SseEvent> sse(SseEvent event) {
        return ServerSentEvent.<SseEvent>builder().data(event).build();
    }

    /**
     * 测试 接口
     * @param question
     * @return
     */
    public String ask(String question) {
        log.info("===收到问题：[{}]", question);
        String context = buildContext(retrieveAndRerank(question).getTopDocs());
        log.info("=== Context 长度: {}", context.length());
        String fullPrompt = "参考资料：\n" + context + "\n\n问题：" + question;
        log.info("=== 完整Prompt尾部: ...{}", fullPrompt.substring(Math.max(0, fullPrompt.length() - 200)));
        // 拼接处改造
        String userMessage = """
        === 参考资料开始 ===
        %s
        === 参考资料结束 ===

        ### 用户问题开始 ###
        %s
        ### 用户问题结束 ###

        请回答上面【用户问题】中的提问。
        """.formatted(context, question);
        return chatClient.prompt().system(SYSTEM_PROMPT).user(userMessage).call().content();
    }
}
