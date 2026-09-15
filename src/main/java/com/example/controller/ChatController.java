package com.example.controller;

import com.example.service.ChatService;
import com.example.service.RerankService;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liusg
 * @version 1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
public class ChatController {
//    private final RagChatService ragChatService;

    private final ChatService chatService;

    private final RerankService rerankService;

    @Autowired
    private VectorStore vectorStore;


    @RequestMapping("/ordinaryAsk")
    public String ordinaryAsk(@RequestParam String question) {
        return chatService.ordinaryAsk(question);

    }

    @RequestMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatService.ask(question);

    }

    /**
     * 普通调用百炼 API
     * @param message
     * @return
     */
//    @GetMapping("/hello")
//    public String hello(@RequestParam String message) {
//        return chatClient.prompt(message).call().content();
//    }
//
//    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> stream(@RequestParam String message, ChatModel chatModel){
//        ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
//
//        return chatClient.prompt(message).stream()
//                .content();
//    }

}
