package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liusg
 * @version 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentIngestionService {

    @Value("${file.path}")
    private String relativePath;

    private final VectorStore vectorStore;

    /**
     * @describe: 导入知识库文档，这个方法应该在启动时调用，或者手动触发
     * @param:
     * @return: void
     * @author LiuShug
     */
    public void ingest() throws Exception {
        // 定义要导入的文档
        Path basePath   = Paths.get(DocumentIngestionService.class.getResource("/").toURI());
        Path targetPath = basePath.resolve(relativePath);
        List<String> filePaths =Files.walk(targetPath)
                .filter(Files::isRegularFile) // 只保留普通文件，自动忽略所有文件夹
                .map(path -> basePath.relativize(path).toString()) // 截取相对路径
                .map(path -> path.replace("\\","/"))
                .collect(Collectors.toList());

        TokenTextSplitter splitter = new TokenTextSplitter();
//        int totalChunks = 0;
        filePaths.forEach(filePath -> {
            // 1. 读取文件
            TextReader reader = new TextReader(new ClassPathResource(filePath));
            reader.getCustomMetadata().put("source", filePath);
            List<Document> documents = reader.get();

            // 2. 切分文档
            List<Document> chunks = splitter.split(documents);

            // 3. 给每一个块添加元数据
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", filePath);
                chunk.getMetadata().put("chunk_size", chunk.getText().length());
            });
            // 4. 添加到向量数据库中
            // 内部自动：调用Embedding 模型 -> 向量模型 -> 向量数据库
            vectorStore.add(chunks);
            System.out.println(filePath);
        });
        log.info("向量数据库导入完成，文档数量：{}", filePaths.size());

    }

}
