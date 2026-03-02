package com.example.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Feedback Agent 配置
 *
 * 该 Agent 将作为子 Agent 在多 Agent 协作场景中使用（如 SequentialAgent、ParallelAgent 等）
 */
@Slf4j
@Configuration
public class FeedbackAgent {

    @Autowired(required = false)
    private FeedBackMcpToolFilter feedBackMcpToolFilter;

    @Bean("feedbackSubAgentBean")
    public ReactAgent feedbackSubAgentBean(
            ChatModel chatModel,
            @Value("classpath:feedback-instruction.txt") Resource feedbackResource,
            ToolCallbackProvider toolsProvider
            ) throws IOException {

        // 手动过滤 MCP 工具：只保留 feedback 或 memory 开头的工具
        List<ToolCallback> McpTools = new ArrayList<>();
        for (ToolCallback toolCallback : toolsProvider.getToolCallbacks()) {
            String toolName = toolCallback.getToolDefinition().name();
            // 只添加 feedback 或 memory 开头的工具
            if (toolName.startsWith("feedback") || toolName.startsWith("memory")) {
                log.info("feedback_agent add mcp tool name: {}", toolName);
                McpTools.add(toolCallback);
            } else {
                log.debug("feedback_agent skip mcp tool name: {}", toolName);
            }
        }
        log.info("MCP 工具数量: {}, 本地工具: 无", McpTools.size());

        var reactAgent = ReactAgent.builder()
                .name("feedback_agent")  // Agent 名称，用于多 Agent 协作时引用
                .description("海风咖啡店的智能反馈处理助手，负责客户反馈收集、情感分析、问题分类和反馈处理")  // Agent 描述，用于 LLM 路由
                .model(chatModel)
                .instruction(feedbackResource.getContentAsString(StandardCharsets.UTF_8));


        // 添加 MCP 工具
        if(!McpTools.isEmpty()){
            reactAgent.tools(McpTools);
        }

        return reactAgent.build();
    }
}
