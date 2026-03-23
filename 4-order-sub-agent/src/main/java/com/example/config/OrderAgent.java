package com.example.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.agent.tools.PythonTool;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
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
public class OrderAgent {

    @Autowired(required = false)
    private OrderMcpToolFilter orderMcpToolFilter;

    @Bean("orderSubAgentBean")
    public ReactAgent orderSubAgentBean(
            ChatModel chatModel,
            @Value("classpath:order-instruction.txt") Resource customerResource,
            ToolCallbackProvider toolsProvider
    ) throws IOException {

        // 手动过滤 MCP 工具：只保留 order 或 memory 开头的工具
        List<ToolCallback> McpTools = new ArrayList<>();
        for (ToolCallback toolCallback : toolsProvider.getToolCallbacks()) {
            String toolName = toolCallback.getToolDefinition().name();
            // 只添加 order 或 memory 开头的工具
            if (toolName.startsWith("order") || toolName.startsWith("memory")) {
                log.info("order_agent add mcp tool name: {}", toolName);
                McpTools.add(toolCallback);
            } else {
                log.debug("order_agent skip mcp tool name: {}", toolName);
            }
        }

        log.info("MCP 工具数量: {}", McpTools.size());

        // 创建 SkillRegistry 用于加载 Skills
        // 使用模块特定的路径避免冲突：skills/{module-name}
        SkillRegistry skillRegistry = ClasspathSkillRegistry.builder()
                .classpathPath("skills/order-agent")
                .build();
        log.info("=============Skills==============");
        skillRegistry.listAll().forEach(System.out::print);
//        同名技能项目覆盖用户
//        SkillRegistry registry = FileSystemSkillRegistry.builder()
//                .userSkillsDirectory("/home/user/saa/skills")
//                .projectSkillsDirectory("/app/project/skills")
//                .build();

        // 3. Shell Hook：提供 Shell 命令执行（工作目录可指定，如当前工程目录）


        ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("user.dir")).build())
                .build();

        var reactAgent = ReactAgent.builder()
                .name("order_agent")  // Agent 名称，用于多 Agent 协作时引用
                .description("海风咖啡店的智能订单处理助手，负责菜单/商品解析、库存与价格校验、订单草案与下单流程，支持订单统计分析")  // Agent 描述，用于 LLM 路由
                .model(chatModel)
                .instruction(customerResource.getContentAsString(StandardCharsets.UTF_8));

        // 添加 MCP 工具
        if(!McpTools.isEmpty()){
            reactAgent.tools(McpTools);
        }

        // 添加 Python 工具
        reactAgent.tools(PythonTool.createPythonToolCallback(PythonTool.DESCRIPTION));

        // 添加 Skills Hook 和 Shell Hook
        SkillsAgentHook skillsHook = SkillsAgentHook.builder()
                .skillRegistry(skillRegistry)
                .autoReload(true)//系统会自动检测 Skill 资源的变化并重新加载，无需重启应用即可让修改后的 Skill 生效
                .build();
        reactAgent.hooks(skillsHook, shellHook);


        return reactAgent.build();
    }
}
