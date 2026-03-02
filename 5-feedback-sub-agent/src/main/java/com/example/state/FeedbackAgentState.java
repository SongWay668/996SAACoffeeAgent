package com.example.state;

import org.springframework.ai.chat.messages.Message;

import java.util.*;

/**
 * FeedbackAgent 状态管理类
 */
public class FeedbackAgentState {

    public static final String MESSAGES_KEY = "messages";
    public static final String CONVERSATION_ID_KEY = "conversation_id";
    public static final String ORIGINAL_QUERY_KEY = "original_query";
    public static final String ENHANCED_QUERY_KEY = "enhanced_query";

    /**
     * 构建初始状态
     */
    public static Map<String, Object> buildInitialState(List<Message> messages, String conversationId) {
        Map<String, Object> state = new HashMap<>();
        state.put(MESSAGES_KEY, messages);
        state.put(CONVERSATION_ID_KEY, conversationId);
        return state;
    }

    /**
     * 获取增强查询
     */
    public static String getEnhancedQuery(Map<String, Object> state) {
        return (String) state.get(ENHANCED_QUERY_KEY);
    }

    /**
     * 设置增强查询
     */
    public static void setEnhancedQuery(Map<String, Object> state, String enhancedQuery) {
        state.put(ENHANCED_QUERY_KEY, enhancedQuery);
    }
}
