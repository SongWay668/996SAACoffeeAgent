package com.example.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SupervisorAgent Controller
 *
 * 使用混合路由策略：
 * 1. 使用 LlmRoutingAgent 进行智能路由到 consult_agent（consult_agent 工作正常）
 * 2. 对于 order_agent 和 feedback_agent，使用关键词路由直接调用（避免工具调用 bug）
 */

public class SupervisorAgentControllerbk {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorAgentControllerbk.class);
    private final LlmRoutingAgent supervisorAgent;
    private final ReactAgent consultAgent;
    private final ReactAgent orderAgent;
    private final ReactAgent feedbackAgent;

    public SupervisorAgentControllerbk(
            @Qualifier("supervisorAgentBean") LlmRoutingAgent supervisorAgent,
            @Qualifier("consultSubAgentBean") ReactAgent consultAgent,
            @Qualifier("orderSubAgentBean") ReactAgent orderAgent,
            @Qualifier("feedbackSubAgentBean") ReactAgent feedbackAgent) {
        this.supervisorAgent = supervisorAgent;
        this.consultAgent = consultAgent;
        this.orderAgent = orderAgent;
        this.feedbackAgent = feedbackAgent;
    }

    /**
     * 流式聊天（Streamable-HTTP）
     * 直接返回文本流，不使用 SSE 格式
     *
     * @param chat_id 对话ID
     * @param user_query 用户查询
     * @param user_id 用户ID
     * @return Flux<String> 文本流
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(
            @RequestParam String chat_id,
            @RequestParam String user_query,
            @RequestParam(required = false) String user_id) {

        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("收到请求（流式） - requestId: {}, chat_id: {}, user_id: {}, query: {}",
                requestId, chat_id, user_id, user_query);

        try {
            // 先判断是否为 order 或 feedback 请求（使用关键词路由）
            ReactAgent directAgent = routeByKeyword(user_query, user_id);

            if (directAgent != null) {
                // 直接调用子 agent（order_agent 或 feedback_agent）
                logger.info("使用关键词路由到: {}, requestId: {}", directAgent.getClass().getSimpleName(), requestId);
                return executeAgent(directAgent, user_query, user_id, chat_id, requestId);
            }

            // 其他情况使用 LlmRoutingAgent 路由（主要是 consult_agent）
            logger.info("使用 LlmRoutingAgent 智能路由, requestId: {}", requestId);

            // 构建输入 - 包含 messages 字段，同时兼容 consult_agent (chat_id) 和 order/feedback_agent (conversation_id)
            List<Message> messages = new ArrayList<>();
            String userInput = user_query;
            if (user_id != null && !user_id.isEmpty()) {
                userInput = String.format("[用户ID: %s]\n%s", user_id, user_query);
            }
            messages.add(new UserMessage(userInput));

            Map<String, Object> input = new HashMap<>();
            input.put("input", user_query);
            input.put("messages", messages);
            input.put("user_id", user_id != null ? user_id : "");
            input.put("chat_id", chat_id);          // consult_agent 使用
            input.put("conversation_id", chat_id);  // order_agent 和 feedback_agent 使用

            return supervisorAgent.stream(input)
                    .doOnNext(output -> {
                        logger.info("supervisorAgent 输出 - node: '{}', type: {}, class: {}",
                                output.node(), output.getClass().getSimpleName(), output.getClass().getName());
                        if (output instanceof StreamingOutput) {
                            StreamingOutput streamingOutput = (StreamingOutput) output;
                            String chunk = streamingOutput.chunk();
                            if (chunk != null && !chunk.trim().isEmpty()) {
                                logger.info("流式输出片段 - requestId: {}, chunk: {}",
                                        requestId, chunk.length() > 100 ? chunk.substring(0, 100) + "..." : chunk);
                            }
                        }
                    })
                    .filter(output -> output instanceof StreamingOutput)
                    .cast(StreamingOutput.class)
                    .map(output -> {
                        String chunk = output.chunk();
                        return chunk != null ? chunk : "";
                    })
                    .filter(chunk -> !chunk.trim().isEmpty())
                    .filter(chunk -> !chunk.equals("Agent State: submitted"))
                    .doOnComplete(() -> logger.info("流式响应完成 - requestId: {}, chat_id: {}", requestId, chat_id))
                    .doOnError(e -> {
                        logger.error("流式输出错误 - requestId: {}, chat_id: {}, error: {}", requestId, chat_id, e.getClass().getName());
                        logger.error("错误详情 - message: {}", e.getMessage());
                        logger.error("错误堆栈: ", e);
                    });

        } catch (Exception e) {
            logger.error("流式处理失败 - requestId: {}, chat_id: {}", requestId, chat_id, e);
            throw new RuntimeException("系统处理出现错误,请稍后重试。", e);
        }
    }

    /**
     * 基于关键词路由到 order_agent 或 feedback_agent
     * 这些 agent 通过 LlmRoutingAgent 调用时工具调用有 bug，需要直接调用
     */
    private ReactAgent routeByKeyword(String query, String userId) {
        String lowerQuery = query.toLowerCase();

        // 订单相关关键词
        if (containsAny(lowerQuery, "来一杯", "来一个", "点单", "下单", "购买", "买", "我要", "帮我做", "做一杯", "我要点")) {
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("订单操作需要提供用户ID，请在页面的用户ID输入框中填写。");
            }
            logger.info("关键词匹配到 order_agent");
            return orderAgent;
        }

        // 反馈/投诉相关关键词
        if (containsAny(lowerQuery, "投诉", "反馈", "差评", "问题", "不满", "意见", "不好喝", "太难喝", "难喝")) {
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("反馈/投诉需要提供用户ID，请在页面的用户ID输入框中填写。");
            }
            logger.info("关键词匹配到 feedback_agent");
            return feedbackAgent;
        }

        return null; // 使用 LlmRoutingAgent 智能路由
    }

    /**
     * 直接执行子 agent（order_agent 或 feedback_agent）
     */
    private Flux<String> executeAgent(ReactAgent agent, String userQuery, String userId, String chatId, String requestId) {
        try {
            List<Message> messages = new ArrayList<>();
            String userInput = userQuery;
            if (userId != null && !userId.isEmpty()) {
                userInput = String.format("[用户ID: %s]\n%s", userId, userQuery);
            }
            messages.add(new UserMessage(userInput));

            Map<String, Object> inputState = new HashMap<>();
            inputState.put("input", userQuery);
            inputState.put("messages", messages);
            inputState.put("user_id", userId != null ? userId : "");
            inputState.put("chat_id", chatId);
            inputState.put("conversation_id", chatId);

            logger.info("直接调用 {} - requestId: {}", agent.getClass().getSimpleName(), requestId);
            return agent.stream(inputState)
                    .doOnNext(output -> logger.info("Agent 输出 - node: '{}', type: {}, class: {}",
                            output.node(), output.getClass().getSimpleName(), output.getClass().getName()))
                    .filter(output -> output instanceof StreamingOutput)
                    .cast(StreamingOutput.class)
                    .map(output -> {
                        String chunk = output.chunk();
                        return chunk != null ? chunk : "";
                    })
                    .filter(chunk -> !chunk.trim().isEmpty())
                    .filter(chunk -> !chunk.equals("Agent State: submitted"))
                    .doOnNext(chunk -> logger.info("流式输出片段 - requestId: {}, chunk: {}",
                            requestId, chunk.length() > 100 ? chunk.substring(0, 100) + "..." : chunk))
                    .doOnComplete(() -> logger.info("流式响应完成 - requestId: {}, chat_id: {}", requestId, chatId))
                    .doOnError(e -> {
                        logger.error("流式输出错误 - requestId: {}, chat_id: {}, error: {}", requestId, chatId, e.getClass().getName());
                        logger.error("错误详情 - message: {}", e.getMessage());
                    });
        } catch (Exception e) {
            logger.error("执行 Agent 失败 - requestId: {}, agent: {}", requestId, agent.getClass().getSimpleName(), e);
            throw new RuntimeException("系统处理出现错误,请稍后重试。", e);
        }
    }

    /**
     * 检查字符串是否包含任意关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

}
