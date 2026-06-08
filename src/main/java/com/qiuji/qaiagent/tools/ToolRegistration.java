package com.qiuji.qaiagent.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolRegistration {

    @Bean
    public List<Object> allTools() {
        return List.of(
                new FileOperationTool(),
                new WebSearchTool(),           // ✅ 自动注入 API Key
                new ResourceDownloadTool(),
                new PDFGenerationTool()
        );
    }
}