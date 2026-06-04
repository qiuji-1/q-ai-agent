package com.qiuji.qaiagent.chatmemory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 基于 JSON 文件的 ChatMemoryRepository 实现。
 */
@Slf4j
public class JsonFileChatMemoryRepository implements ChatMemoryRepository {

    /** 存储目录路径 */
    private final Path storageDir;

    /**
     * 序列化工具，配置了多态类型处理以支持 Message 的多种子类
     * （AssistantMessage / UserMessage / SystemMessage / ToolResponseMessage）
     */
    private final ObjectMapper objectMapper;

    /**
     * @param storagePath 存储目录的路径，如 "temp/chat-memory"
     */
    public JsonFileChatMemoryRepository(String storagePath) {
        this.storageDir = Paths.get(storagePath);
        this.objectMapper = buildObjectMapper();
        initStorageDir();
    }

    /**
     * 构建支持 Message 多态序列化的 ObjectMapper。
     */
    private static ObjectMapper buildObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Message.class)     // 只信任 Message 及其子类
                .build();

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    /** 确保存储目录存在，不存在则创建 */
    private void initStorageDir() {
        try {
            Files.createDirectories(storageDir);
            log.info("JSON 文件记忆存储目录: {}", storageDir.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录: " + storageDir, e);
        }
    }

    // ==================== ChatMemoryRepository 接口方法 ====================

    /**
     * 列出所有已存储的对话 ID。
     * 遍历目录下所有 .json 文件，去掉后缀即为 conversationId。
     */
    @Override
    public List<String> findConversationIds() {
        if (!Files.exists(storageDir)) return Collections.emptyList();
        try (Stream<Path> files = Files.list(storageDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".json"))
                    .map(name -> name.substring(0, name.length() - 5))   // 去掉 ".json" 后缀
                    .toList();
        } catch (IOException e) {
            log.error("读取对话列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 读取指定对话的全部历史消息。
     * 文件不存在时返回空列表（而非抛异常），与官方 InMemory 实现行为一致。
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        File file = getFile(conversationId);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            List<Message> messages = objectMapper.readValue(file,
                    new TypeReference<List<Message>>() {});   // 用 TypeReference 保留泛型信息
            log.debug("读取对话记忆: {} -> {} 条消息", conversationId, messages.size());
            return messages;
        } catch (IOException e) {
            log.error("读取对话记忆失败: {}", conversationId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 保存（覆盖写入）指定对话的全部消息。
     * 使用 PrettyPrinter 输出格式化 JSON，便于人工查看和调试。
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        File file = getFile(conversationId);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, messages);
            log.debug("保存对话记忆: {} -> {} 条消息", conversationId, messages.size());
        } catch (IOException e) {
            log.error("保存对话记忆失败: {}", conversationId, e);
        }
    }

    /**
     * 删除指定对话的记忆文件。
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        try {
            Files.deleteIfExists(getFile(conversationId).toPath());
            log.debug("删除对话记忆: {}", conversationId);
        } catch (IOException e) {
            log.error("删除对话记忆失败: {}", conversationId, e);
        }
    }

    // ==================== 内部工具方法 ====================

    /** 根据 conversationId 拼接出对应的 .json 文件 */
    private File getFile(String conversationId) {
        return storageDir.resolve(conversationId + ".json").toFile();
    }
}
