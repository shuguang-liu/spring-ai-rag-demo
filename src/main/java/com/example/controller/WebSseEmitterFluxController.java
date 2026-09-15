package com.example.controller;

import com.example.dto.EvalResult;
import com.example.dto.SseEvent;
import com.example.service.WebSseEmitterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author liushug
 * @date 2026/9/14 22:13
 * @description
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class WebSseEmitterFluxController {

    private final WebSseEmitterService webSseEmitterService;

    @GetMapping(value = "/ask/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter askStream(@RequestParam String question){
        System.out.println("================askStream");
        SseEmitter sseEmitter = new SseEmitter(0L); // 0 = 不超时
        webSseEmitterService.askStreamWithEmitter(question, sseEmitter);
        return sseEmitter;
    }

    @GetMapping(value = "/ask/stream/flux", produces = "text/event-stream;charset=UTF-8")
    public Flux<ServerSentEvent<SseEvent>> askStreamFlux(@RequestParam String question) {
        System.out.println("=================askStreamFlux");
        return webSseEmitterService.askStreamFlux(question);
    }

    @GetMapping("/eval/ask")
    public String ask(@RequestParam String question) throws IOException {
        System.out.println("=================evalOne");
        return webSseEmitterService.ask(question);
    }

    @PostMapping("/eval/batch")
    public String evalBatch() throws IOException {
        System.out.println("=================evalBatch");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> testet = objectMapper.readValue(new File("docs/testset.json"), new TypeReference<>() {});
        List<EvalResult> results = testet.stream().map(t -> webSseEmitterService.evalOne(t.get("question"), t.get("ground_truth"))).toList();
        System.out.println("评估完成");
        objectMapper.writeValue(new File("docs/eval_result.json"), results);
        System.out.println("结果保存完成");
        return "评估完成，共 " + results.size() + " 条";
    }


}
