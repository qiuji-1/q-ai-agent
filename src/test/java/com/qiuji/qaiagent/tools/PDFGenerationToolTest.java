package com.qiuji.qaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFGenerationToolTest {
    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "Spring AI 稳定版 1.1.7.pdf";
        String content = "Spring AI 官方文档 https://spring.io/";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
