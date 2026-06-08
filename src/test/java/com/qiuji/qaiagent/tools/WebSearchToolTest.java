package com.qiuji.qaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Autowired
    private WebSearchTool webSearchTool;

    @Test
    void testSearch() {
        String query = "百度一下你就知道";
        String result = webSearchTool.search(query);
        
        assertNotNull(result, "搜索结果不应为空");
        assertTrue(result.contains("Search results for:"), "结果应包含搜索标题");
        assertTrue(result.contains(query), "结果应包含搜索关键词");
        
        System.out.println("搜索结果：\n" + result);
    }

    @Test
    void testSearchWithEnglishQuery() {
        String query = "Spring AI";
        String result = webSearchTool.search(query);
        
        assertNotNull(result, "英文搜索结果不应为空");
        assertTrue(result.contains(query) || result.toLowerCase().contains(query.toLowerCase()), 
                   "结果应包含搜索关键词");
        
        System.out.println("英文搜索结果：\n" + result);
    }
}