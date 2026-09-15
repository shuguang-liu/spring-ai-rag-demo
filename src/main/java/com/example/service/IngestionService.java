package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author liusg
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class IngestionService {

    private final VectorStore vectorStore;
    public String ingest(MultipartFile file) throws IOException {
        Resource resource = new InputStreamResource(file.getInputStream());
        // 1. 读取文件 (TikaDocumentReader 支持 PDF/Word/TXT/Markdown)
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocs = reader.read();

        // 2. 切块（TokenTextSplitter 按 token数切， 默认chunk 约800 token, 重叠100）
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(rawDocs);
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("source", file.getOriginalFilename());
            chunks.get(i).getMetadata().put("chunk_size", i);
        }
        // 3. 添加到向量数据库中
        vectorStore.add(chunks);
        return "上传成功，共生成 " + chunks.size() + " 个 chunk";

    }
}
