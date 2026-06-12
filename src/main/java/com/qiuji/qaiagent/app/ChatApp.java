package com.qiuji.qaiagent.app;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.qiuji.qaiagent.advisor.MyLoggerAdvisor;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class ChatApp {


    private final ChatClient chatClient;

    @Resource
    private VectorStore chatAppVectorStore;

    @Resource
    private List<Object> allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    private static final String SYSTEM_PROMPT = "你是一位资深语言沟通专家和高情商对话顾问，擅长帮助用户解决各类社交场景中的沟通难题。"
            +"你的核心使命是通过深度理解用户的对话情境、关系背景和真实意图，提供个性化、可操作的高情商回复建议。"+"在每次回复前，你必须先通过引导性问题深入了解用户，例如：\"对方是你的什么关系？\"\"对话发生在什么情境下？\"\"你希望达到什么效果？\"\"对方的情绪状态如何？\"等关键信息。"+
            "你的回复应遵循\"理解确认→情境分析→引导提问→多方案建议→原理解释→后续提醒\"的结构，为用户提供2-3种不同风格（保守稳妥型/真诚直接型/幽默轻松型/深度共情型）的回复选项，并解释每种回复为何有效。"
            +"你需要具备情绪识别、语言艺术和场景适配三大核心能力，能够识别言外之意、掌握得体表达技巧、适配职场/情感/家庭/社交等不同场景。"
            +"你的对话风格应温暖亲切、专业但不生硬，像一位值得信赖的朋友。"
            +"记住：永远不要急于给出\"标准答案\"，先确保充分理解情境；每个建议都要解释背后的逻辑，帮助用户提升沟通能力；如果用户描述模糊，主动追问细节；尊重用户真实想法，不强制灌输观点。";

    /**
     *  构造器注入 —— ChatMemory 由 Spring AI + JDBC 自动配置，无需手写
     */
    public ChatApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        log.info("✅ 使用 {} 持久化对话记忆", chatMemory.getClass().getSimpleName());
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                   //     ,new MyLoggerAdvisor()
                )
                .build();
    }

    /*
    /**
     * 初始化 ChatClient 内存存储
     * @param dashscopeChatModel
     /
    // 使用 @Qualifier 指定注入 DashScope 的 ChatModel
    public ChatApp( ChatModel dashscopeChatModel){
        // 创建内存存储仓库
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

        // 创建 MessageWindowChatMemory，设置最大保留消息数
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
              .chatMemoryRepository(chatMemoryRepository)
              .maxMessages(10)
              .build();

        // 使用 ChatMemoryAdvisor.create() 方法创建 advisor
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志adviso
                        new MyLoggerAdvisor()
//                        // 自定义 Re2 Advisor
//                       new ReReadingAdvisor()
                )
                .build();
    }
*/
    /**
     * AI 基础对话（支持多轮对话）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（SSE 流式输出）
     * @param message 用户消息
     * @param chatId  对话ID
     * @return Flux<String> 流式文本块
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    record ChatReport(String title, List<String> suggest){

    }
    /**
     * AI 聊天报告内容（结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public ChatReport doChatWithReport(String message, String chatId) {
       ChatReport chatReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话结束后，生成本次对话的聊天报告，标题为{用户名}的聊天报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(ChatReport.class);
        log.info("report: {}", chatReport);
        return chatReport;
    }

    /**
     * AI RAG 知识库问答（基于文档检索的增强生成）
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId){
            ChatResponse chatResponse = chatClient
                    .prompt()
                    .user(message)
                    .advisors(spec -> spec
                            .param(ChatMemory.CONVERSATION_ID, chatId))
                    .advisors(new MyLoggerAdvisor())
                    .advisors(QuestionAnswerAdvisor.builder(chatAppVectorStore).build())
                    .call()
                    .chatResponse();
            String content = chatResponse.getResult().getOutput().getText();
            log.info("RAG 回答: {}", content);
            return content;
    }

    /**
     * AI 工具调用对话（支持文件操作、搜索、下载、PDF生成等）
     * @param message 用户消息
     * @param chatId 对话ID
     * @return AI回复内容
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools.toArray())
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("工具调用结果: {}", content);
        return content;
    }

    /**
     * AI MCP 工具调用对话（通过 MCP 协议连接 GitHub ）
     * 使用 ToolCallbackProvider 自动获取配置的 MCP 服务器提供的所有工具
     * 
     * @param message 用户消息
     * @param chatId 对话ID
     * @return AI回复内容
     */
    public String doChatWithMcp(String message, String chatId) {
        log.info(" 开始 MCP 工具调用 - message: {}, chatId: {}", message, chatId);
        
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())  // 获取 MCP 动态工具列表
                .call()
                .chatResponse();
        
        String content = response.getResult().getOutput().getText();
        log.info(" MCP 工具调用完成 - content: {}", content);
        return content;
    }

}
