package com.example.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentRetrieverService {

    private final VectorStore vectorStore;

    public DocumentRetrieverService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String searchKnowledge(String query) {
        log.info("=== DocumentRetrieverService.searchKnowledge 开始 ===");
        log.info("查询参数 - query: {}", query);
        log.info("配置 - similarityThreshold: 0.5, topK: 5");
        try {
            List<Document> docs = vectorStore.similaritySearch(
                    org.springframework.ai.vectorstore.SearchRequest.builder()
                            .query(query)
                            .similarityThreshold(0.3)
                            .topK(5)
//                            .filterExpression("")
                            .build()
            );
            log.info("检索到文档数量: {}", docs.size());
            for (int i = 0; i < Math.min(3, docs.size()); i++) {
                Document doc = docs.get(i);
                String preview = doc.getText() != null && doc.getText().length() > 100
                        ? doc.getText().substring(0, 100) + "..."
                        : doc.getText();
                log.info("文档[{}] - 预览: {}", i, preview);
            }
            String content = docs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining(" "));
            log.info("=== DocumentRetrieverService.searchKnowledge 结束 - 返回长度: {} ===", content.length());
            return content;
        } catch (Exception e) {
            log.error("DocumentRetrieverService.searchKnowledge 异常", e);
            throw e;
        }
    }
}

