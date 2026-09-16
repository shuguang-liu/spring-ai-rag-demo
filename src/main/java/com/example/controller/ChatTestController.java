package com.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author liushug
 * @description TODO
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ChatTestController {

    private final ChatClient chatClient;

    /**
     * 普通对话接口（同步）
     *
     * @param message   用户消息
     * @param sessionId 会话ID（用于区分不同用户的多轮对话）
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message, @RequestParam(defaultValue = "default") String sessionId){
        return chatClient.prompt().user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();
    }

    /**
     * 流式对话接口（SSE）
     * 实时返回AI生成的内容，用户体验更好
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        sessionId))
                .stream()
                .content();
    }

}
