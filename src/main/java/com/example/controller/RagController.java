package com.example.controller;

import com.example.service.DocumentIngestionService;
import com.example.service.RagChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author liusg
 * @version 1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RagController {
    private final RagChatService ragChatService;

    private final DocumentIngestionService documentIngestionService;

    /**
     * 知识库问答接口
     * @param question
     * @return
     */
    @GetMapping("/ask")
    public String ask(@RequestParam String question){
        return ragChatService.ask(question);
    }

    /**
     * 手动触发文档导入
     * @return
     */
    @PostMapping("/ingest")
    public String ingestAll() throws Exception {
        documentIngestionService.ingest();
        return "导入成功 ";
    }
}
