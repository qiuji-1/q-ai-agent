package com.qiuji.qaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化向量数据库
 */
@Slf4j
@Component
public class ChatAppVectorStoreConfig {
    @Bean
    public VectorStore chatAppVectorStore(
            EmbeddingModel embeddingModel,
            ChatAppDocumentLoader chatAppDocumentLoader) {
            log.info("开始初始化向量数据库...");
            SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        try {
            // 加载markdown文档
            List<Document> allDocuments = chatAppDocumentLoader.loadMarkdown();
            log.info("成功加载 {} 个文档", allDocuments.size());
            // 直接向量化并存储（跳过文本分割步骤）
            vectorStore.add(allDocuments);
            log.info("成功将文档存入向量数据库");
        } catch (Exception e) {
            log.error("初始化向量数据库失败", e);
            throw new RuntimeException("向量数据库初始化失败: " + e.getMessage(), e);
        }
        log.info("向量数据库初始化完成");
        return vectorStore;
    }
}
