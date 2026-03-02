package com.example.service;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.example.state.FeedbackAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * 反馈 Agent 服务 - 处理咖啡店反馈相关的智能对话
 *
 * 功能：
 * - 客户反馈收集和整理
 * - 情感分析和问题分类
 * - 反馈记录和归档
 * - 投诉处理和问题跟进
 */
@Service
public class FeedbackAgentService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackAgentService.class);

    @Autowired
    @Qualifier("feedbackSubAgentBean")
    private ReactAgent feedbackAgent;

    /**
     * 流式反馈对话 - 返回 Flux<String> 支持非阻塞流式输出
     *
     * @param conversationId 对话ID（每个用户会话唯一）
     * @param userInput 用户输入
     * @param userId 用户ID（必填）
     * @return 流式响应 Flux
     */
    public Flux<String> streamFeedback(String conversationId, String userInput, String userId) throws GraphRunnerException {
        logger.info("流式反馈 - conversationId: {}, userId: {}, input: {}", conversationId, userId, userInput);

        // 1. 构建输入状态：使用 FeedbackAgentState 构建
        List<org.springframework.ai.chat.messages.Message> inputMessages = new ArrayList<>();
        // 将 userId 作为用户消息的一部分传递
        String finalUserInput = userInput;
        if (userId != null && !userId.isEmpty()) {
            finalUserInput = String.format("[用户ID: %s]\n%s", userId, userInput);
        }
        inputMessages.add(new UserMessage(finalUserInput));

        Map<String, Object> inputState = FeedbackAgentState.buildInitialState(inputMessages, conversationId);
        inputState.put(FeedbackAgentState.ORIGINAL_QUERY_KEY, userInput);
        inputState.put("user_id", userId);

        // 2. 用于收集完整响应
        StringBuilder fullResponse = new StringBuilder();

        // 3. 执行 ReactAgent.stream() 并返回 Flux
        logger.info("开始执行 Agent.stream - conversationId: {}", conversationId);
        return feedbackAgent.stream(inputState)
                .doOnNext(output -> logger.info("Agent 输出 - node: '{}', type: {}", output.node(), output.getClass().getSimpleName()))
                .filter(output -> output instanceof StreamingOutput)
                .cast(StreamingOutput.class)
                .map(output -> {
                    String chunk = output.chunk();
                    return chunk != null ? chunk : "";
                })
                .filter(chunk -> !chunk.trim().isEmpty())
                .filter(chunk -> {
                    // 过滤掉重复的完整响应
                    boolean isDuplicate = chunk.length() > 100 && fullResponse.length() > 50
                            && chunk.contains(fullResponse.toString());
                    if (isDuplicate) {
                        logger.info("过滤重复完整响应 - chunk 长度: {}, 已累积长度: {}", chunk.length(), fullResponse.length());
                    }
                    return !isDuplicate;
                })
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                    logger.debug("流式输出片段 - conversationId: {}, chunk: {}", conversationId, chunk);
                })
                .doOnComplete(() -> {
                    logger.info("流式响应完成 - conversationId: {}, 总长度: {}", conversationId, fullResponse.length());
                })
                .doOnError(e -> logger.error("流式输出错误 - conversationId: {}", conversationId, e));
    }
}
