package com.qiuji.qaiagent.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;

import com.qiuji.qaiagent.agent.model.AgentState;
import com.qiuji.qaiagent.tools.TerminateTool;
import com.qiuji.qaiagent.tools.WebSearchTool;

/**
 * Agent 智能体逻辑测试（不调用实际大模型）
 */
public class AgentLogicTest {

    @Test
    void testAgentInitialization() {
        // 创建基础代理实例
        BaseAgent agent = new BaseAgent() {
            @Override
            public String step() {
                return "测试步骤";
            }
        };
        
        // 设置属性
        agent.setName("TestAgent");
        agent.setSystemPrompt("Test system prompt");
        agent.setMaxSteps(10);
        
        // 验证属性设置
        assertEquals("TestAgent", agent.getName());
        assertEquals("Test system prompt", agent.getSystemPrompt());
        assertEquals(10, agent.getMaxSteps());
        assertEquals(0, agent.getCurrentStep());
        assertEquals(AgentState.IDLE, agent.getState());
        
        // 验证消息列表初始化
        assertNotNull(agent.getMessageList());
        assertTrue(agent.getMessageList().isEmpty());
        
        System.out.println("✅ Agent初始化测试通过");
    }

    @Test
    void testMessageListManagement() {
        BaseAgent agent = new BaseAgent() {
            @Override
            public String step() {
                return "测试步骤";
            }
        };
        
        // 添加测试消息
        UserMessage testMessage = new UserMessage("测试消息");
        agent.getMessageList().add(testMessage);
        
        // 验证消息添加成功
        assertEquals(1, agent.getMessageList().size());
        assertEquals("测试消息", ((UserMessage) agent.getMessageList().get(0)).getText());
        
        // 清空消息列表
        agent.setMessageList(new ArrayList<>());
        assertEquals(0, agent.getMessageList().size());
        
        System.out.println("✅ 消息列表管理测试通过");
    }

    @Test
    void testStateTransitions() {
        BaseAgent agent = new BaseAgent() {
            @Override
            public String step() {
                return "测试步骤";
            }
        };
        
        // 初始状态应为IDLE
        assertEquals(AgentState.IDLE, agent.getState());
        
        // 手动设置状态为RUNNING
        agent.setState(AgentState.RUNNING);
        assertEquals(AgentState.RUNNING, agent.getState());
        
        // 手动设置状态为FINISHED
        agent.setState(AgentState.FINISHED);
        assertEquals(AgentState.FINISHED, agent.getState());
        
        // 手动设置状态为ERROR
        agent.setState(AgentState.ERROR);
        assertEquals(AgentState.ERROR, agent.getState());
        
        System.out.println("✅ 状态转换测试通过");
    }

    @Test
    void testToolList() {
        // 创建工具列表
        List<Object> tools = new ArrayList<>();
        tools.add(new WebSearchTool());
        tools.add(new TerminateTool());
        
        // 创建ToolCallAgent
        ToolCallAgent agent = new ToolCallAgent(tools.toArray());
        
        // 验证工具数量
        assertEquals(2, agent.getAvailableTools().length);
        
        // 验证工具类型
        assertTrue(agent.getAvailableTools()[0] instanceof WebSearchTool);
        assertTrue(agent.getAvailableTools()[1] instanceof TerminateTool);
        
        System.out.println("✅ 工具列表测试通过");
    }

    @Test
    void testReActAgentInheritance() {
        // 创建ReActAgent实例
        ReActAgent agent = new ReActAgent() {
            @Override
            public boolean think() {
                return true;
            }
            
            @Override
            public String act() {
                return "行动";
            }
        };
        
        // 验证继承关系
        assertTrue(agent instanceof BaseAgent);
        
        // 测试think和act方法
        assertTrue(agent.think());
        assertEquals("行动", agent.act());
        
        // 测试step方法
        String result = agent.step();
        assertNotNull(result);
        
        System.out.println("✅ ReActAgent继承测试通过");
    }

    @Test
    void testAgentStateEnum() {
        // 验证枚举值
        assertEquals(4, AgentState.values().length);
        assertEquals(AgentState.IDLE, AgentState.valueOf("IDLE"));
        assertEquals(AgentState.RUNNING, AgentState.valueOf("RUNNING"));
        assertEquals(AgentState.FINISHED, AgentState.valueOf("FINISHED"));
        assertEquals(AgentState.ERROR, AgentState.valueOf("ERROR"));
        
        System.out.println("✅ AgentState枚举测试通过");
    }
}
