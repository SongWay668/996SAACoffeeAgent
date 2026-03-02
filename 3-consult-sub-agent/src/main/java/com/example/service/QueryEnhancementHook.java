package com.example.service;


import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.example.state.ConsultAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 查询增强 Hook
 *
 * 在 Agent 执行前对用户查询进行增强，例如：
 * 1. 查询重写/改写
 * 2. 同义词扩展
 * 3. 上下文注入
 */
@HookPositions({HookPosition.BEFORE_AGENT})
public class QueryEnhancementHook extends AgentHook {

    private static final Logger logger = LoggerFactory.getLogger(QueryEnhancementHook.class);
    private static final String ENHANCED_QUERY_KEY = ConsultAgentState.ENHANCED_QUERY_KEY;

    private final ChatModel chatModel;
    private final boolean enabled;

    public QueryEnhancementHook(ChatModel chatModel) {
        this(chatModel, true);
    }

    public QueryEnhancementHook(ChatModel chatModel, boolean enabled) {
        this.chatModel = chatModel;
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return "query_enhancement";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        if (!enabled) {
            logger.debug("查询增强 Hook 已禁用，跳过处理");
            return CompletableFuture.completedFuture(null);
        }

        try {
            // 1. 从状态中提取用户查询
            Optional<List<Message>> messagesOptional = state.value(ConsultAgentState.MESSAGES_KEY);
            if (messagesOptional.isEmpty()) {
                logger.warn("状态中没有找到消息，跳过查询增强");
                return CompletableFuture.completedFuture(null);
            }

            List<Message> messages = messagesOptional.get();
            if (messages.isEmpty()) {
                logger.warn("消息列表为空，跳过查询增强");
                return CompletableFuture.completedFuture(null);
            }

            // 获取最后一条用户消息
            String userQuery = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                if (msg instanceof UserMessage) {
                    userQuery = msg.getText();
                    break;
                }
            }

            if (userQuery == null || userQuery.trim().isEmpty()) {
                logger.debug("未找到有效的用户查询，跳过增强");
                return CompletableFuture.completedFuture(null);
            }

            logger.info("开始查询增强 - 原始查询: {}", userQuery);

            // 2. 调用 LLM 进行查询增强
            String enhancementPrompt = buildEnhancementPrompt(userQuery);
            ChatResponse response = chatModel.call(new Prompt(enhancementPrompt));

            String enhancedQuery = response.getResult() != null
                    ? response.getResult().getOutput().getText()
                    : userQuery;

            if (enhancedQuery == null || enhancedQuery.trim().isEmpty()) {
                logger.warn("查询增强返回空结果，使用原始查询");
                enhancedQuery = userQuery;
            }

            logger.info("查询增强完成 - 原始: {}, 增强: {}", userQuery, enhancedQuery);

            // 3. 返回增强后的查询，将被添加到状态中
            Map<String, Object> result = new HashMap<>();
            result.put(ENHANCED_QUERY_KEY, enhancedQuery);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            logger.error("查询增强失败", e);
            // 返回 null，不影响后续 Agent 执行
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 构建查询增强的 Prompt
     */
    private String buildEnhancementPrompt(String userQuery) {
        return String.format("""
                你是一个咖啡店咨询助手的查询增强专家。
                
                任务：将用户的原始查询转换为更清晰、更完整、更具体的查询，以便更好地获取相关信息。
                
                原始查询：%s
                
                要求：
                1. 保持查询的原意和意图
                2. 补充缺失的上下文信息（如果明显）
                3. 使用更专业的词汇（针对咖啡相关）
                4. 输出增强后的查询，不要包含任何解释
                5. 如果原始查询已经很清晰，保持不变
                
                请直接输出增强后的查询，不要包含其他内容。
                """, userQuery);
    }
}
