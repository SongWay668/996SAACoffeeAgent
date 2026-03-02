package com.example.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * Agent Service
 *
 * [DEPRECATED] 不再使用，请使用 ConsultService
 *
 * 使用 ChatMemoryService 实现 ReactAgent 的会话保持
 *
 * 使用方式：
 * 手动调用 chatMemoryService 管理会话状态
 */
@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Autowired
    @Qualifier("consultSubAgentBean")
    private ReactAgent consultAgent;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatMemoryService chatMemoryService;

    /**
     * 手动管理会话的调用方式
     *
     * 1. 调用 Agent（单次调用，不处理历史）
     * 2. 保存用户消息和 AI 响应
     * 3. 可选：修剪会话
     */
    public String chat(String chatId, String userMessage) {
        logger.info("收到消息 - chatId: {}, message: {}", chatId, userMessage);

        try {
            // 1. 调用 ReactAgent
            Object result = consultAgent.call(userMessage);
            logger.info("ReactAgent 返回类型: {}, 值: {}", 
                result != null ? result.getClass().getName() : "null", 
                result);

            String responseText;

            if (result instanceof AssistantMessage) {
                responseText = ((AssistantMessage) result).getText();
            } else if (result instanceof String) {
                responseText = (String) result;
            } else if (result instanceof Map) {
                Map<?, ?> resultMap = (Map<?, ?>) result;
                Object outputValue = resultMap.get("consult_result");
                if (outputValue instanceof String) {
                    responseText = (String) outputValue;
                } else if (outputValue instanceof List) {
                    List<?> list = (List<?>) outputValue;
                    responseText = list.isEmpty() ? "" : list.get(0).toString();
                } else {
                    responseText = resultMap.toString();
                }
            } else if (result instanceof List) {
                List<?> list = (List<?>) result;
                if (list.isEmpty()) {
                    responseText = "";
                } else {
                    Object firstItem = list.get(0);
                    if (firstItem instanceof AssistantMessage) {
                        responseText = ((AssistantMessage) firstItem).getText();
                    } else if (firstItem instanceof String) {
                        responseText = (String) firstItem;
                    } else if (firstItem instanceof Map) {
                        Map<?, ?> resultMap = (Map<?, ?>) firstItem;
                        Object outputValue = resultMap.get("consult_result");
                        responseText = outputValue != null ? outputValue.toString() : resultMap.toString();
                    } else {
                        responseText = list.toString();
                    }
                }
            } else {
                responseText = result != null ? result.toString() : "";
            }

            if (responseText == null || responseText.isEmpty()) {
                responseText = "";
            }

            // 2. 手动保存到 Redis
            chatMemoryService.addUserMessage(chatId, userMessage);
            chatMemoryService.addAssistantMessage(chatId, responseText);

            // 3. 检查是否需要修剪（参考文档的上下文管理）
            if (chatMemoryService.shouldTrimOrSummarize(chatId, 4000)) {
                chatMemoryService.trimMessages(chatId, 10);
                logger.info("已修剪会话 - chatId: {}", chatId);
            }

            logger.info("Agent 响应 - chatId: {}, response: {}", chatId, responseText);
            return responseText;

        } catch (Exception e) {
            logger.error("调用 Agent 失败 - chatId: {}", chatId, e);
            return "抱歉，处理您的请求时出错：" + e.getMessage();
        }
    }

    /**
     * 流式调用 Agent（使用 ChatModel 真正流式）
     */
    public Flux<String> streamChat(String chatId, String userMessage) {
        logger.info("收到消息（流式） - chatId: {}, message: {}", chatId, userMessage);

        // 获取历史消息列表
        List<Message> history = chatMemoryService.getConversationHistory(chatId);
        logger.info("当前历史消息数 - chatId: {}, count: {}", chatId, history.size());

        // 保存用户消息
        chatMemoryService.addUserMessage(chatId, userMessage);

        // 创建请求消息列表：历史消息 + 新用户消息
        List<Message> messages = new ArrayList<>(history);
        messages.add(new UserMessage(userMessage));

        // 用于收集完整响应
        StringBuilder fullResponse = new StringBuilder();

        // ChatModel.stream() 使用 Prompt 对象
        // 先 windowTimeout 聚合字符流，每 100ms 或 30 个字符为一个批次
        return chatModel.stream(new Prompt(messages))
                .map(chatResponse -> chatResponse.getResult() != null
                        ? chatResponse.getResult().getOutput().getText()
                        : "")
                .filter(content -> content != null && !content.isEmpty())
                .map(content -> {
                    // 如果内容已经包含 "data:" 前缀，去掉它（避免重复）
                    if (content.startsWith("data:")) {
                        return content.substring(5);
                    }
                    return content;
                })
                .windowTimeout(30, java.time.Duration.ofMillis(100))
                .flatMap(window -> window.collectList().map(list -> String.join("", list)))
                .filter(text -> !text.isEmpty())  // 过滤掉空的聚合结果
                .doOnNext(text -> {
                    fullResponse.append(text);
                    logger.debug("流式输出片段 - chatId: {}, text: {}", chatId, text);
                })
                .doOnComplete(() -> {
                    // 流式输出完成后，保存完整响应到内存
                    String response = fullResponse.toString();
                    chatMemoryService.addAssistantMessage(chatId, response);

                    // 检查是否需要修剪
                    if (chatMemoryService.shouldTrimOrSummarize(chatId, 4000)) {
                        chatMemoryService.trimMessages(chatId, 10);
                    }
                })
                .doOnError(e -> logger.error("流式输出错误 - chatId: {}", chatId, e));
    }

    /**
     * 查看会话历史
     */
    public List<String> getHistory(String chatId) {
        return chatMemoryService.getConversationHistory(chatId).stream()
                .map(msg -> msg.getMessageType().name() + ": " + msg.getText())
                .toList();
    }

    /**
     * 清空会话
     */
    public void clearChat(String chatId) {
        chatMemoryService.clearConversation(chatId);
        logger.info("清空会话 - chatId: {}", chatId);
    }

    /**
     * 获取会话统计信息
     */
    public String getConversationStats(String chatId) {
        int messageCount = chatMemoryService.getConversationLength(chatId);
        int estimatedTokens = chatMemoryService.estimateTokens(
                chatMemoryService.getConversationHistory(chatId)
        );
        
        return String.format(
                "会话统计 - chatId: %s\n消息数: %d\n预估 Token: %d",
                chatId, messageCount, estimatedTokens
        );
    }
}
