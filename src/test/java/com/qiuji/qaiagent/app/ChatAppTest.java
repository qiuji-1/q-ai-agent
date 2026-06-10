package com.qiuji.qaiagent.app;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;


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
    @Test
    void doChatWithRag(){
        String chatId = UUID.randomUUID().toString();
        String message = "今天我的朋友升职了，我该如何跟他聊天？";
        String answer = chatApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Test
    void doChatWithTools(){
        // 测试联网搜索问题的答案
        testMessage("请帮我搜索一下'Spring AI 1.1.7的新特性？");
        // 测试资源下载：图片下载
        testMessage("请帮我下载 Google 的 Logo 图片，URL是 https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png ，文件名保存为 google-logo.png");
        // 测试文件操作：保存用户档案
        testMessage("请将以下内容保存到文件 user-profile.txt：姓名：秋霁；职业：AI工程师；爱好：编程");
        // 测试 PDF 生成
        testMessage("请生成一份名为 'ai-report.pdf' 的PDF报告，内容为：人工智能发展趋势报告 - 2026年版");
    }
    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = chatApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Test
    void doChatWithMcp(){
        String chatId = UUID.randomUUID().toString();

        // 测试 1：先查看仓库根目录结构（确认分支名）
       // testMcpMessage(
       //     "请列出 qiuji-1/q-ai-agent 仓库根目录的所有文件和文件夹（使用 master 分支）",
       //     chatId
       // );

        // 测试 2：读取 ChatApp.java 文件
      // testMcpMessage(
       //     "请在 master 分支上读取 qiuji-1/q-ai-agent 仓库中 src/main/java/com/qiuji/qaiagent/app/ChatApp.java 文件的完整内容",
      //      chatId
     //   );
        
        // 测试 3：提交今天的代码
       // testMcpMessage(
       //     "请将以下内容提交到 qiuji-1/q-ai-agent 仓库的 src/test/mcp-test-result.txt 文件：" +
       //     "'MCP 测试成功！\n测试时间：" + new java.util.Date() + "\n测试内容：验证 GitHub MCP 集成'",
       //     chatId
       // );

        //测试4 图片搜索MCP
        //testMcpMessage(
        //        "请帮我搜索一些咖啡馆图片",
        //        chatId
        //);
    }
    private void testMcpMessage(String message, String chatId) {
        System.out.println("用户请求: " + message);
        String answer = chatApp.doChatWithMcp(message, chatId);
        System.out.println("AI 回复: " + answer);
        System.out.println("----------------------------------------");
        Assertions.assertNotNull(answer);
    }
}