package com.qiuji.qaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileOperationToolTest {

        @Test
        public void testReadFile() {
            FileOperationTool tool = new FileOperationTool();
            String fileName = "今日说法.txt";
            String result = tool.readFile(fileName);
            assertNotNull(result);
        }

        @Test
        public void testWriteFile() {
            FileOperationTool tool = new FileOperationTool();
            String fileName = "今日说法.txt";
            String content = "今天干了什么有意义的事情呢";
            String result = tool.writeFile(fileName, content);
            assertNotNull(result);
        }
    }
