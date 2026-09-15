package com.example.controller;

import com.example.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author liusg
 * @version 1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    @RequestMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        // 1. 读取文件 (TikaDocumentReader 支持 PDF/Word/TXT/Markdown)

        String result =ingestionService.ingest(file);
        return result;
    }


}
