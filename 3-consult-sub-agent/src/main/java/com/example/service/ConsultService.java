package com.example.service;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.example.state.ConsultAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import reactor.core.publisher.Flux;

/**
 * 咨询服务 - 通过状态手动管理 ChatMemory
 */
@Service
public class ConsultService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultService.class);

    @Autowired
    @Qualifier("consultSubAgentBean")
    private ReactAgent consultAgent;

    @Autowired
    private ChatMemory chatMemory;

    /**
     * 咨询对话 - 每次调用都从 ChatMemory 读取历史，执行后保存
     *
     * @param conversationId 对话ID（每个用户会话唯一）
     * @param userInput 用户输入
     * @param userId 用户ID（可选）
     * @return AI 响应
     */
    public String consult(String conversationId, String userInput, String userId) throws GraphRunnerException {
        // 1. 从 Redis ChatMemory 获取历史消息
        List<Message> history = chatMemory.get(conversationId);

        // 2. 构建输入状态：使用 ConsultAgentState 构建
        List<Message> inputMessages = new ArrayList<>(history);
        // 如果有 userId，将其作为用户消息的一部分传递
        String finalUserInput = userInput;
        if (userId != null && !userId.isEmpty()) {
            finalUserInput = String.format("[用户ID: %s]\n%s", userId, userInput);
        }
        inputMessages.add(new UserMessage(finalUserInput));

        Map<String, Object> inputState = ConsultAgentState.buildInitialState(inputMessages, conversationId);
        inputState.put(ConsultAgentState.ORIGINAL_QUERY_KEY, userInput);
        if (userId != null && !userId.isEmpty()) {
            inputState.put("user_id", userId);
        }

        // 3. 执行 ReactAgent.stream() 并收集响应
        StringBuilder responseBuilder = new StringBuilder();
        consultAgent.stream(inputState)
                .filter(output -> "llm".equals(output.node()) && output instanceof StreamingOutput)
                .cast(StreamingOutput.class)
                .map(StreamingOutput::chunk)
                .filter(chunk -> chunk != null && !chunk.trim().isEmpty())
                .doOnNext(responseBuilder::append)
                .blockLast(); // 阻塞等待完整响应

        String response = responseBuilder.toString();

        // 4. 保存用户消息和 AI 响应到 ChatMemory
        chatMemory.add(conversationId, List.of(new UserMessage(userInput)));
        chatMemory.add(conversationId, List.of(new AssistantMessage(response)));

        return response;
    }

    /**
     * 流式咨询对话 - 返回 Flux<String> 支持非阻塞流式输出
     *
     * @param conversationId 对话ID（每个用户会话唯一）
     * @param userInput 用户输入
     * @param userId 用户ID（可选）
     * @return 流式响应 Flux
     */
    public Flux<String> streamConsult(String conversationId, String userInput, String userId) throws GraphRunnerException {
        logger.info("流式咨询 - conversationId: {}, userId: {}, input: {}", conversationId, userId, userInput);

        // 1. 从 ChatMemory 获取历史消息
        List<Message> history = chatMemory.get(conversationId);
        logger.info("历史消息数 - conversationId: {}, count: {}", conversationId, history.size());

        // 2. 检查是否需要修剪
        if (shouldTrimOrSummarize(conversationId, 4000)) {
            trimMessages(conversationId, 10);
            history = chatMemory.get(conversationId); // 重新获取修剪后的历史
            logger.info("已修剪会话 - conversationId: {}, 新消息数: {}", conversationId, history.size());
        }

        // 3. 构建输入状态：使用 ConsultAgentState 构建
        List<Message> inputMessages = new ArrayList<>(history);
        // 如果有 userId，将其作为用户消息的一部分传递
        String finalUserInput = userInput;
        if (userId != null && !userId.isEmpty()) {
            finalUserInput = String.format("[用户ID: %s]\n%s", userId, userInput);
        }
        inputMessages.add(new UserMessage(finalUserInput));

        Map<String, Object> inputState = ConsultAgentState.buildInitialState(inputMessages, conversationId);
        inputState.put(ConsultAgentState.ORIGINAL_QUERY_KEY, userInput);
        if (userId != null && !userId.isEmpty()) {
            inputState.put("user_id", userId);
        }

        // 4. 保存用户消息到 ChatMemory
        chatMemory.add(conversationId, List.of(new UserMessage(userInput)));

        // 5. 用于收集完整响应
        StringBuilder fullResponse = new StringBuilder();

        // 6. 执行 ReactAgent.stream() 并返回 Flux,核心方法
        logger.info("开始执行 Agent.stream - conversationId: {}", conversationId);
        return consultAgent.stream(inputState)
//                .doOnNext(output -> logger.info("Agent 输出 - node: '{}', type: {}", output.node(), output.getClass().getSimpleName()))
                .filter(output -> output instanceof StreamingOutput)
                .cast(StreamingOutput.class)
                .map(output -> {
                    String chunk = output.chunk();
                    return chunk != null ? chunk : "";
                })
                .filter(chunk -> !chunk.trim().isEmpty())
                .filter(chunk -> {
                    // 过滤掉重复的完整响应（ReactAgent 在流式结束时可能会重复发送完整内容）
                    // 如果 chunk 包含完整响应且比之前累积的内容长很多，说明是重复的
                    boolean isDuplicate = chunk.length() > 100 && fullResponse.length() > 50
                            && chunk.contains(fullResponse.toString());
                    if (isDuplicate) {
                        logger.info("过滤重复完整响应 - chunk 长度: {}, 已累积长度: {}", chunk.length(), fullResponse.length());
                    }
                    return !isDuplicate;
                })
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
//                    logger.debug("流式输出片段 - conversationId: {}, chunk: {}", conversationId, chunk);
                })
                .doOnComplete(() -> {
                    // 流式输出完成后，保存完整响应到 ChatMemory
                    String response = fullResponse.toString();
                    logger.info("保存 AI 响应到 ChatMemory - conversationId: {}, 长度: {}, 前100字符: {}",
                            conversationId, response.length(),
                            response.length() > 100 ? response.substring(0, 100) : response);
                    chatMemory.add(conversationId, List.of(new AssistantMessage(response)));

                    // 记录增强查询信息（如果有）
                    String enhancedQuery = ConsultAgentState.getEnhancedQuery(inputState);
                    if (enhancedQuery != null && !enhancedQuery.equals(userInput)) {
                        logger.info("查询增强 - 原始: {}, 增强: {}", userInput, enhancedQuery);
                    }

                    logger.info("流式响应完成 - conversationId: {}, 总长度: {}", conversationId, response.length());
                })
                .doOnError(e -> logger.error("流式输出错误 - conversationId: {}", conversationId, e));
    }

    /**
     * 清空对话历史
     */
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
    }

    /**
     * 获取对话历史
     */
    public List<Message> getHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }

    /**
     * 获取会话长度
     */
    public int getConversationLength(String conversationId) {
        return chatMemory.get(conversationId).size();
    }

    /**
     * 估算 Token 数量（简化版）
     * 用于判断是否需要修剪或总结
     */
    public int estimateTokens(List<Message> messages) {
        // 简单估算：每个字符约 0.25 token（粗略估计）
        int totalChars = messages.stream()
                .mapToInt(m -> m.getText() != null ? m.getText().length() : 0)
                .sum();
        return (int) (totalChars * 0.25);
    }

    /**
     * 检查是否需要修剪或总结
     * 参考文档的上下文窗口管理
     */
    public boolean shouldTrimOrSummarize(String conversationId, int maxTokens) {
        int estimatedTokens = estimateTokens(chatMemory.get(conversationId));
        boolean shouldAction = estimatedTokens > maxTokens;

        if (shouldAction) {
            logger.warn("会话 {} 的 token 数量 {} 超过限制 {}，需要修剪或总结",
                      conversationId, estimatedTokens, maxTokens);
        }

        return shouldAction;
    }

    /**
     * 修剪消息（参考文档的修剪策略）
     * 保留第一条系统消息和最后 N 条消息
     */
    public void trimMessages(String conversationId, int maxKeep) {
        List<Message> allMessages = chatMemory.get(conversationId);

        if (allMessages.size() <= maxKeep) {
            logger.debug("会话 {} 消息数 {} 不需要修剪（maxKeep: {}）", conversationId, allMessages.size(), maxKeep);
            return; // 无需修剪
        }

        // 保留第一条（通常是系统消息）和最后 maxKeep 条
        Message firstMessage = allMessages.get(0);
        int startIndex = allMessages.size() - maxKeep;
        List<Message> trimmedMessages = allMessages.subList(startIndex, allMessages.size());

        // 如果第一条是系统消息，保留它
        if (firstMessage instanceof SystemMessage && !trimmedMessages.contains(firstMessage)) {
            trimmedMessages = new ArrayList<>(trimmedMessages);
            trimmedMessages.add(0, firstMessage);
        }

        // 清空并重新保存
        chatMemory.clear(conversationId);
        chatMemory.add(conversationId, trimmedMessages);

        logger.info("修剪消息 - conversationId: {}, 从 {} 条减少到 {} 条",
                   conversationId, allMessages.size(), trimmedMessages.size());
    }

    /**
     * 获取会话统计信息
     */
    public String getConversationStats(String conversationId) {
        int messageCount = getConversationLength(conversationId);
        int estimatedTokens = estimateTokens(chatMemory.get(conversationId));

        return String.format(
                "会话统计 - conversationId: %s\n消息数: %d\n预估 Token: %d",
                conversationId, messageCount, estimatedTokens
        );
    }

//    /**
//     * 根据查询内容检索知识库
//     */
//    public String searchKnowledge(String query) {
//        logger.info("=== ConsultService.searchKnowledge 入口 ===");
//        logger.info("请求参数 - query: {}", query);
//
//        try {
//            DashScopeDocumentRetrieverOptions options = DashScopeDocumentRetrieverOptions.builder().
//                    withEnableReranking(enableReranking).
//                    withRerankTopN(rerankTopN).
//                    withRerankMinScore(rerankMinScore).
//                    build();
//            List<Document> documents = dashscopeApi.retriever(indexID, query, options);
//
//            logger.info("检索到文档数量: {}", documents.size());
//
//            if (documents.isEmpty()) {
//                String result = "未找到相关资料，查询内容：" + query;
//                logger.info("=== ConsultService.searchKnowledge 出口 ===");
//                logger.info("返回结果: {}", result);
//                return result;
//            }
//
//            // 整合所有文档的text内容，用\n\n作为分隔符
//            StringBuilder result = new StringBuilder();
//            for (int i = 0; i < documents.size(); i++) {
//                Document document = documents.get(i);
//                String text = document.getText();
//
//                if (!text.trim().isEmpty()) {
//                    result.append(text);
//
//                    // 如果不是最后一个文档，添加分隔符
//                    if (i < documents.size() - 1) {
//                        result.append("\n\n");
//                    }
//                }
//            }
//
//            String finalResult = result.toString();
//            logger.info("=== ConsultService.searchKnowledge 出口 ===");
//            logger.info("返回结果长度: {} 字符", finalResult.length());
//            logger.info("返回结果预览: {}", finalResult.length() > 200 ? finalResult.substring(0, 200) + "..." : finalResult);
//
//            return finalResult;
//        } catch (Exception e) {
//            logger.error("知识库检索异常", e);
//            String errorResult = "知识库检索失败: " + e.getMessage() + "，查询内容：" + query;
//            logger.info("=== ConsultService.searchKnowledge 出口 ===");
//            logger.info("返回错误结果: {}", errorResult);
//            return errorResult;
//        }
//    }
}
