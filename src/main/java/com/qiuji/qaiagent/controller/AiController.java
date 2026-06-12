package com.qiuji.qaiagent.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.qiuji.qaiagent.agent.QiuManus;
import com.qiuji.qaiagent.app.ChatApp;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ChatApp chatApp;

    @Resource
    private List<Object> allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * SSE 流式对话接口
     * @param message 用户消息
     * @param chatId  对话ID
     * @return SseEmitter
     */
    @GetMapping("/chat/sse")
    public SseEmitter doChatSse(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时

        // 获取 Flux 数据流并直接订阅
        chatApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );

        return emitter;
    }

    /**
     * SSE 流式调用 QiuManus 超级智能体
     * @param message 用户消息
     * @return SseEmitter
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        QiuManus qiuManus = new QiuManus(allTools, dashscopeChatModel);
        return qiuManus.runStream(message);
    }
}
