package com.qiuji.qaiagent.agent;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * QiuManus AI超级智能体
 * 继承自ToolCallAgent，具备完整的工具调用能力
 */
@Component
@Slf4j
public class QiuManus extends ToolCallAgent {

    public QiuManus(List<Object> allTools, ChatModel chatModel) {
        super(allTools.toArray());
        this.setName("QiuManus");

        String SYSTEM_PROMPT = """
                You are QiuManus, an all-capable AI assistant.
                You have various tools to complete complex requests.
                Use tools to solve problems step by step.
                Call doTerminate when task is completed.
                Respond in Chinese.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """
                Proactively select appropriate tools for user needs.
                For complex tasks, break down and use different tools step by step.
                Use the terminate tool to end when finished.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);

        ChatClient chatClient = ChatClient.builder(chatModel).build();
        this.setChatClient(chatClient);

        log.info("QiuManus agent initialized");
    }
}
