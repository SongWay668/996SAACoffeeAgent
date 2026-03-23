package com.example.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.example.service.AnswerValidationInterceptor;
import com.example.service.QueryEnhancementHook;
import com.example.tool.ConsultTools;
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
 * Consult Agent 配置
 *
 * 该 Agent 将作为子 Agent 在多 Agent 协作场景中使用（如 SequentialAgent、ParallelAgent 等）
 */
@Slf4j
@Configuration
public class ConsultAgent {

    @Autowired(required = false)
    private ConsultMcpToolFilter consultMcpToolFilter;

    @Bean
    public ReactAgent consultSubAgentBean(
            ChatModel chatModel,
            @Value("classpath:consult-instruction.txt") Resource customerResource,
            ConsultTools consultTools,
            ToolCallbackProvider toolsProvider
            ) throws IOException {

        // 手动过滤 MCP 工具：只保留 consult 或 memory 开头的工具
        List<ToolCallback> McpTools = new ArrayList<>();
        for (ToolCallback toolCallback : toolsProvider.getToolCallbacks()) {
            String toolName = toolCallback.getToolDefinition().name();
            // 只添加 consult 或 memory 开头的工具
            if (toolName.startsWith("consult") || toolName.startsWith("memory")) {
                log.info("consult_agent add mcp tool name: {}", toolName);
                McpTools.add(toolCallback);
            } else {
                log.debug("consult_agent skip mcp tool name: {}", toolName);
            }
        }
        log.info("MCP 工具数量: {}", McpTools.size());

        // 打印本地工具名称
        log.info("consult_agent 本地工具名称: consult-search-knowledge");

        // ========== Skills 配置 ==========
        // 创建 Skills Registry - 从 classpath 加载技能
        SkillRegistry skillRegistry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();

        // 创建 Skills Agent Hook
        // SkillsAgentHook 会自动注册 read_skill 工具，并在系统提示中注入技能列表
        SkillsAgentHook skillsHook = SkillsAgentHook.builder()
                .skillRegistry(skillRegistry)
                .build();

        log.info("Consult Agent 已启用 Skills 支持，技能目录: classpath:skills");
        log.info("可用技能数量: {}", skillRegistry.size());

        var reactAgent = ReactAgent.builder()
                .name("consult_agent")  // Agent 名称，用于多 Agent 协作时引用
                .description("咖啡店咨询助手，专门处理咖啡产品推荐、价格查询、口味咨询、活动规则、促销信息等客户问题")  // Agent 描述，用于 LLM 路由
                .model(chatModel)
                .instruction(customerResource.getContentAsString(StandardCharsets.UTF_8));

        // 添加本地工具方法
        reactAgent.methodTools(consultTools);

        // 添加 MCP 工具
        if(!McpTools.isEmpty()){
            reactAgent.tools(McpTools);
        }

        // ========== 添加 Skills Hook ==========
        // SkillsAgentHook 提供 read_skill 工具，并注入技能列表到系统提示
        reactAgent.hooks(skillsHook);

//                .hooks(new QueryEnhancementHook(chatModel))
//                .interceptors(new AnswerValidationInterceptor());


        return reactAgent.build();
    }
}
