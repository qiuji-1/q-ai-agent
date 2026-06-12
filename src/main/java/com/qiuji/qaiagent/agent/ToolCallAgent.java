package com.qiuji.qaiagent.agent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;

import com.qiuji.qaiagent.agent.model.AgentState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final Object[] availableTools;
    private ChatResponse toolCallChatResponse;
    private boolean nextStepPromptAdded = false;

    public ToolCallAgent(Object[] availableTools) {
        super();
        this.availableTools = availableTools;
    }

    @Override
    public boolean think() {
        if (!nextStepPromptAdded && getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
            nextStepPromptAdded = true;
        }

        try {
            ChatClient chatClient = getChatClient();
            if (chatClient == null) {
                throw new RuntimeException("ChatClient is not initialized");
            }
            ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                    .internalToolExecutionEnabled(false)  // 禁用自动执行
                    .build();

            ChatResponse chatResponse = chatClient.prompt()
                    .messages(getMessageList())
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .options(options)    // ← 传入配置
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            getMessageList().add(assistantMessage);

            if (toolCallList != null && !toolCallList.isEmpty()) {
                boolean terminateToolCalled = toolCallList.stream()
                        .anyMatch(toolCall -> "doTerminate".equals(toolCall.name()));
                if (terminateToolCalled) {
                    setState(AgentState.FINISHED);
                    return false;
                }
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error(getName() + " think error: " + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        // think() 已把 assistantMessage 加入 messageList，
        // 但 executeToolCalls 内部也会添加 assistantMessage，会导致重复。
        // 先移除 last message（即 think() 添加的 assistantMessage），避免重复。
        getMessageList().remove(getMessageList().size() - 1);

        // 使用 ToolCallingManager 执行工具调用，传入实际可用的工具回调
        ToolCallback[] callbacks = ToolCallbacks.from(availableTools);
        StaticToolCallbackResolver resolver = new StaticToolCallbackResolver(Arrays.asList(callbacks));
        ToolCallingManager toolCallingManager = ToolCallingManager.builder()
                .toolCallbackResolver(resolver)
                .build();
        Prompt prompt = new Prompt(getMessageList());
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // conversationHistory 已包含助手消息和工具调用返回结果，替换 messageList
        setMessageList(toolExecutionResult.conversationHistory());

        // 获取工具执行结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));

        // 检查是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        log.info(results);
        return results;
    }
}
