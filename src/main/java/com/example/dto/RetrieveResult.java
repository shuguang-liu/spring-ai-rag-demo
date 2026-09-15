package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author liushug
 * @date 2026/9/14 22:11
 * @description 检索结果
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RetrieveResult {

    private List<Document> candidates;

    private List<Document> topDocs;

}
