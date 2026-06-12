package com.qiuji.qaiagent.agent;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * QiuManus 智能体集成测试类
 * 测试完整的智能体功能，需要真实的API调用
 */
@SpringBootTest
@Slf4j
class QiuManusIntegrationTest {

    @Resource
    private QiuManus qiuManus;

    /**
     * 测试完整的智能体执行流程
     */
    @Test
    void run() {
        String userPrompt = "我的好朋友居住在上海宝山区，请帮我找到 5 公里内合适的娱乐地点，并结合一些网络图片，制定一份详细的游玩计划，并以 PDF 格式输出，名称带上日期";
        String answer = qiuManus.run(userPrompt);
        Assertions.assertNotNull(answer);
        log.info("智能体执行结果:\n{}", answer);
    }
}