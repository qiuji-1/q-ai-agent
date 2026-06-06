package com.qiuji.qaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  聊天APP知识库文档加载
 */
@Component
@Slf4j
public class ChatAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public ChatAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 所有markdown文档加载
     * @return
     */
    public List<Document> loadMarkdown() {
        List<Document> allDocuments = new ArrayList<>();
        //加载多篇markdown文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource1 : resources) {
                String fileName = resource1.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource1, config);
                allDocuments.addAll(reader.get());
            }
        } catch (Exception e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
