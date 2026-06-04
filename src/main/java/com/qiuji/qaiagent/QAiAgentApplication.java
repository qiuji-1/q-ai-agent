package com.qiuji.qaiagent;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//(exclude = {DashScopeChatAutoConfiguration.class}) //防止同时运行两个AI模型导致的重复问题
public class  QAiAgentApplication {

    public static void main(String[] args) {
        System.out.println("启动项目的main方法被调用了!");
        SpringApplication.run(QAiAgentApplication.class, args);
    }

}
