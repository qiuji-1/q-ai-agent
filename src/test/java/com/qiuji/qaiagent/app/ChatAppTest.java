package com.qiuji.qaiagent.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import java.util.UUID;


@SpringBootTest
class ChatAppTest {

    @Resource
    private ChatApp chatApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是秋霁";
        String answer = chatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想要更好地跟我的朋友凯伦聊天";
        answer = chatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的朋友叫什么？刚跟你说过，帮我回忆一下";
        answer = chatApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是秋霁,明天我要进行开题答辩，我该怎么跟导师沟通";
        ChatApp.ChatReport chatReport = chatApp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(chatReport);
    }
}