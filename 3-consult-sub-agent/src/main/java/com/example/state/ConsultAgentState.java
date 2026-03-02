package com.example.state;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * Consult Agent 状态定义
 *
 * 使用 Map<String, Object> 作为状态容器，包含以下字段：
 * - messages: 对话消息列表
 * - chat_id: 会话ID
 * - user_id: 用户ID（可选）
 * - original_query: 原始用户查询
 * - enhanced_query: 增强后的查询（由 Hook 生成）
 */
public class ConsultAgentState {

    public static final String MESSAGES_KEY = "messages";
    public static final String CHAT_ID_KEY = "chat_id";
    public static final String USER_ID_KEY = "user_id";
    public static final String ORIGINAL_QUERY_KEY = "original_query";
    public static final String ENHANCED_QUERY_KEY = "enhanced_query";

    /**
     * 构建初始状态
     */
    public static java.util.Map<String, Object> buildInitialState(
            List<Message> messages,
            String chatId) {
        return buildInitialState(messages, chatId, null);
    }

    /**
     * 构建初始状态（包含用户ID）
     */
    public static java.util.Map<String, Object> buildInitialState(
            List<Message> messages,
            String chatId,
            String userId) {
        java.util.Map<String, Object> state = new java.util.HashMap<>();
        state.put(MESSAGES_KEY, messages);
        state.put(CHAT_ID_KEY, chatId);
        if (userId != null) {
            state.put(USER_ID_KEY, userId);
        }
        return state;
    }

    /**
     * 从状态中获取消息列表
     */
    @SuppressWarnings("unchecked")
    public static List<Message> getMessages(java.util.Map<String, Object> state) {
        return (List<Message>) state.get(MESSAGES_KEY);
    }

    /**
     * 从状态中获取会话ID
     */
    public static String getChatId(java.util.Map<String, Object> state) {
        return (String) state.get(CHAT_ID_KEY);
    }

    /**
     * 从状态中获取用户ID
     */
    public static String getUserId(java.util.Map<String, Object> state) {
        return (String) state.get(USER_ID_KEY);
    }

    /**
     * 从状态中获取原始查询
     */
    public static String getOriginalQuery(java.util.Map<String, Object> state) {
        return (String) state.get(ORIGINAL_QUERY_KEY);
    }

    /**
     * 从状态中获取增强查询
     */
    public static String getEnhancedQuery(java.util.Map<String, Object> state) {
        return (String) state.get(ENHANCED_QUERY_KEY);
    }

    /**
     * 设置增强查询到状态
     */
    public static void setEnhancedQuery(java.util.Map<String, Object> state, String enhancedQuery) {
        state.put(ENHANCED_QUERY_KEY, enhancedQuery);
    }
}
