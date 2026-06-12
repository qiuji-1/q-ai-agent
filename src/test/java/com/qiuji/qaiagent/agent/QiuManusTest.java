package com.qiuji.qaiagent.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.qiuji.qaiagent.agent.model.AgentState;

import lombok.extern.slf4j.Slf4j;

/**
 * QiuManus 智能体单元测试类
 * 使用简单的测试方式验证Agent的基本功能
 */
@Slf4j
class QiuManusTest {

    private BaseAgent testAgent;

    @BeforeEach
    void setUp() {
        // 创建一个简单的测试Agent实现
        testAgent = new BaseAgent() {
            @Override
            public String step() {
                // 简单实现，直接返回完成状态
                setState(AgentState.FINISHED);
                return "测试步骤完成";
            }
        };
        testAgent.setName("TestAgent");
        testAgent.setMaxSteps(5);
    }

    /**
     * 测试智能体基本状态管理
     */
    @Test
    void testAgentStateManagement() {
        // 初始状态应该是IDLE
        Assertions.assertEquals(AgentState.IDLE, testAgent.getState());
        
        // 设置状态为RUNNING
        testAgent.setState(AgentState.RUNNING);
        Assertions.assertEquals(AgentState.RUNNING, testAgent.getState());
        
        // 设置状态为FINISHED
        testAgent.setState(AgentState.FINISHED);
        Assertions.assertEquals(AgentState.FINISHED, testAgent.getState());
        
        log.info("Agent state management test passed");
    }

    /**
     * 测试Agent名称和配置
     */
    @Test
    void testAgentConfiguration() {
        testAgent.setName("MyTestAgent");
        testAgent.setMaxSteps(10);
        testAgent.setSystemPrompt("Test system prompt");
        testAgent.setNextStepPrompt("Test next step prompt");
        
        Assertions.assertEquals("MyTestAgent", testAgent.getName());
        Assertions.assertEquals(10, testAgent.getMaxSteps());
        Assertions.assertEquals("Test system prompt", testAgent.getSystemPrompt());
        Assertions.assertEquals("Test next step prompt", testAgent.getNextStepPrompt());
        
        log.info("Agent configuration test passed");
    }

    /**
     * 测试消息列表管理
     */
    @Test
    void testMessageListManagement() {
        Assertions.assertNotNull(testAgent.getMessageList());
        Assertions.assertTrue(testAgent.getMessageList().isEmpty());
        
        // 添加消息后应该不为空
        int initialSize = testAgent.getMessageList().size();
        testAgent.getMessageList().add(new org.springframework.ai.chat.messages.UserMessage("Test message"));
        Assertions.assertEquals(initialSize + 1, testAgent.getMessageList().size());
        
        log.info("Message list management test passed");
    }

    /**
     * 测试AgentState枚举值
     */
    @Test
    void testAgentStateEnum() {
        Assertions.assertEquals(4, AgentState.values().length);
        Assertions.assertTrue(AgentState.valueOf("IDLE") != null);
        Assertions.assertTrue(AgentState.valueOf("RUNNING") != null);
        Assertions.assertTrue(AgentState.valueOf("FINISHED") != null);
        Assertions.assertTrue(AgentState.valueOf("ERROR") != null);
        
        log.info("AgentState enum test passed");
    }

    /**
     * 测试ReActAgent继承结构
     */
    @Test
    void testReActAgentInheritance() {
        ReActAgent reActAgent = new ReActAgent() {
            @Override
            public boolean think() {
                return false;
            }

            @Override
            public String act() {
                return "Test act";
            }
        };
        
        Assertions.assertTrue(reActAgent instanceof BaseAgent);
        Assertions.assertEquals(AgentState.IDLE, reActAgent.getState());
        
        log.info("ReActAgent inheritance test passed");
    }

    /**
     * 测试ToolCallAgent初始化
     */
    @Test
    void testToolCallAgentInitialization() {
        Object[] tools = new Object[]{new Object()};
        ToolCallAgent toolCallAgent = new ToolCallAgent(tools);
        
        Assertions.assertNotNull(toolCallAgent.getAvailableTools());
        Assertions.assertEquals(1, toolCallAgent.getAvailableTools().length);
        
        log.info("ToolCallAgent initialization test passed");
    }
}