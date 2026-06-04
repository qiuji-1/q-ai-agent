package com.qiuji.qaiagent.config;

import com.qiuji.qaiagent.chatmemory.JsonFileChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    /**
     * JSON 文件持久化的 ChatMemoryRepository
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new JsonFileChatMemoryRepository("temp/chat-memory");
    }

    /**
     * 使用文件持久化的 ChatMemory（保留最近20条消息）
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }
}
