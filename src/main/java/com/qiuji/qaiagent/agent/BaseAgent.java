package com.qiuji.qaiagent.agent;

import com.qiuji.qaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    /**
     * 代理名称
     */
    private String name;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 下一步提示词
     */
    private String nextStepPrompt;

    /**
     * 代理执行状态
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 最大执行步骤数
     */
    private int maxSteps = 10;

    /**
     * 当前执行步骤
     */
    private int currentStep = 0;

    /**
     * 大模型客户端
     */
    private ChatClient chatClient;

    /**
     * 消息上下文列表，用于维护会话历史
     */
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }

        if (StringUtils.isEmpty(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 更改状态为运行中
        state = AgentState.RUNNING;
        // 记录用户消息到上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存每步执行结果
        List<String> results = new ArrayList<>();

        try {
            // 循环执行直到达到最大步骤或任务完成
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                
                // 执行单步
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }

            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }

            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误: " + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 流式运行代理（SSE）
     * 每完成一个 think/act 步骤立即推送结果给前端
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L); // Agent 可能多步执行，5分钟超时

        if (this.state != AgentState.IDLE) {
            emitter.completeWithError(new RuntimeException("Cannot run agent from state: " + this.state));
            return emitter;
        }
        if (!StringUtils.hasText(userPrompt)) {
            emitter.completeWithError(new RuntimeException("Cannot run agent with empty user prompt"));
            return emitter;
        }

        // 异步执行，避免阻塞主线程
        Thread.startVirtualThread(() -> {
            try {
                state = AgentState.RUNNING;
                messageList.add(new UserMessage(userPrompt));

                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);

                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    emitter.send(result);
                }

                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    emitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }

                emitter.complete();
            } catch (IOException e) {
                // SSE 连接可能已断开
                log.warn("SSE connection closed: {}", e.getMessage());
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                emitter.completeWithError(e);
            } finally {
                cleanup();
            }
        });

        return emitter;
    }

    /**
     * 执行单个步骤，由子类实现
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源，子类可重写
     */
    protected void cleanup() {
        // 默认空实现，子类可以重写此方法来清理资源
    }
}