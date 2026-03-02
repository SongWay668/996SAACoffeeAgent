package com.example.service;

import com.example.repository.RedisChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ChatMemory Service
 *
 * [DEPRECATED] 不再使用，请使用 Spring AI 原生 ChatMemory
 *
 * 简化版实现，直接使用 RedisChatMemory 管理会话
 *
 * 主要功能：
 * 1. 会话级持久化（conversationId）
 * 2. 消息自动保存和加载
 * 3. 支持上下文窗口管理（修剪、删除、总结）
 */
@Service
public class ChatMemoryService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryService.class);

    @Autowired
    private RedisChatMemory redisChatMemory;

    /**
     * 保存会话消息（用于 ReactAgent 手动管理）
     */
    public void saveMessages(String conversationId, List<Message> messages) {
        if (messages != null && !messages.isEmpty()) {
            redisChatMemory.add(conversationId, messages);
            logger.info("已保存 {} 条消息到 conversationId: {}", messages.size(), conversationId);
        }
    }

    /**
     * 添加用户消息到会话
     */
    public void addUserMessage(String conversationId, String userMessage) {
        Message message = new org.springframework.ai.chat.messages.UserMessage(userMessage);
        redisChatMemory.add(conversationId, List.of(message));
        logger.debug("添加用户消息 - conversationId: {}, message: {}", conversationId, userMessage);
    }

    /**
     * 添加 AI 响应到会话
     */
    public void addAssistantMessage(String conversationId, String assistantMessage) {
        Message message = new org.springframework.ai.chat.messages.AssistantMessage(assistantMessage);
        redisChatMemory.add(conversationId, List.of(message));
        logger.debug("添加 AI 响应 - conversationId: {}, message: {}", conversationId, assistantMessage);
    }

    /**
     * 获取会话历史
     */
    public List<Message> getConversationHistory(String conversationId) {
        return redisChatMemory.get(conversationId);
    }

    /**
     * 获取最近的 N 条消息
     * 用于上下文窗口管理
     */
    public List<Message> getRecentMessages(String conversationId, int lastN) {
        List<Message> allMessages = redisChatMemory.get(conversationId);
        if (allMessages.size() <= lastN) {
            return allMessages;
        }
        return allMessages.subList(allMessages.size() - lastN, allMessages.size());
    }

    /**
     * 清空会话
     */
    public void clearConversation(String conversationId) {
        redisChatMemory.clear(conversationId);
        logger.info("清空会话 - conversationId: {}", conversationId);
    }

    /**
     * 修剪消息（参考文档的修剪策略）
     * 保留第一条系统消息和最后 N 条消息
     */
    public void trimMessages(String conversationId, int maxKeep) {
        List<Message> allMessages = redisChatMemory.get(conversationId);

        if (allMessages.size() <= maxKeep) {
            return; // 无需修剪
        }

        // 保留第一条（通常是系统消息）和最后 maxKeep 条
        Message firstMessage = allMessages.get(0);
        int startIndex = allMessages.size() - maxKeep;
        List<Message> trimmedMessages = allMessages.subList(startIndex, allMessages.size());

        // 清空并重新保存
        redisChatMemory.clear(conversationId);
        redisChatMemory.add(conversationId, trimmedMessages);

        logger.info("修剪消息 - conversationId: {}, 从 {} 条减少到 {} 条",
                   conversationId, allMessages.size(), trimmedMessages.size() + 1);
    }

    /**
     * 检查会话长度
     */
    public int getConversationLength(String conversationId) {
        return redisChatMemory.get(conversationId).size();
    }

    /**
     * 估算 Token 数量（简化版）
     * 用于判断是否需要总结
     */
    public int estimateTokens(List<Message> messages) {
        // 简单估算：每个字符约 0.25 token（粗略估计）
        int totalChars = messages.stream()
                .mapToInt(m -> m.getText().length())
                .sum();
        return (int) (totalChars * 0.25);
    }

    /**
     * 检查是否需要修剪或总结
     * 参考文档的上下文窗口管理
     */
    public boolean shouldTrimOrSummarize(String conversationId, int maxTokens) {
        int estimatedTokens = estimateTokens(redisChatMemory.get(conversationId));
        boolean shouldAction = estimatedTokens > maxTokens;

        if (shouldAction) {
            logger.warn("会话 {} 的 token 数量 {} 超过限制 {}，需要修剪或总结",
                      conversationId, estimatedTokens, maxTokens);
        }

        return shouldAction;
    }
}
