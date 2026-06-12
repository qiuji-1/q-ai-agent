package com.qiuji.qaiagent.tools;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册配置类
 */
@Configuration
public class ToolRegistration {

    @Resource
    private WebSearchTool webSearchTool;  // 注入 Spring 管理的 Bean

    /**
     * 注册所有工具为对象列表，供 Agent 使用
     */
    @Bean
    public List<Object> allTools() {
        return List.of(
                new FileOperationTool(),
                webSearchTool,
                new ResourceDownloadTool(),
                new PDFGenerationTool(),
                new TerminateTool()
        );
    }
}
