package com.qiuji.qaiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试ChatApp的工具调用功能
 */
@SpringBootTest
@Slf4j
class ChatAppToolTest {

    @Resource
    private ChatApp chatApp;

    @Test
    void testToolCall() {
        String message = "请搜索上海市宝山区的游玩地点，并将结果保存到文件中";
        String result = chatApp.doChatWithTools(message, "test-chat-id");
        log.info("工具调用结果: {}", result);
    }
}