package com.qiuji.qaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class LangChainAiInvoke {
    public static void main(String[] args) {
        ChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();
        String answer = qwenChatModel.chat("你好，我是秋霁，我能叫你千问吗");
        System.out.println(answer);
    }
}
