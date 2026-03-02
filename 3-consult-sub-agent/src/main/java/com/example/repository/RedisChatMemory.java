package com.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RedisChatMemory implements ChatMemory {

    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemory.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${chat.memory.prefix:customerchat:}")
    private String memoryPrefix;

    private final ObjectMapper objectMapper;

    public RedisChatMemory() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void add(@NonNull String conversationId, List<Message> messages) {
        String key = memoryPrefix + conversationId;
        for(Message message:messages)
            redisTemplate.opsForList().rightPush(key, formatMessage(message));
        // 设置5分钟过期时间
        redisTemplate.expire(key, 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    @Override
    public List<Message> get(@NonNull String conversationId) {
        List<String> list = redisTemplate.opsForList().range(memoryPrefix + conversationId,0,-1);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<Message> messages = new ArrayList<>();

        for(String msg:list) {
            messages.add(parseMessage(msg));
        }
        return messages;
    }

    @Override
    public void clear(@NonNull String conversationId) {
        redisTemplate.delete(memoryPrefix + conversationId);
    }

    // 格式化消息（使用简单的 | 分隔符，避免 JSON 编码问题）
    private String formatMessage(Message msg) {
        String content;
        try {
            // 尝试不同的获取内容的方式
            if (msg instanceof UserMessage) {
                content = ((UserMessage) msg).getText();
            } else if (msg instanceof AssistantMessage) {
                content = ((AssistantMessage) msg).getText();
            } else if (msg instanceof SystemMessage) {
                content = ((SystemMessage) msg).getText();
            } else {
                content = msg.toString();
            }
        } catch (Exception e) {
            content = "";
        }
        // 直接返回字符串拼接
        return msg.getMessageType().name() + "|" + content;
    }

    // 解析消息（使用简单的 | 分隔符）
    private Message parseMessage(String json) {
        String[] parts = json.split("\\|", 2);
        String type = parts[0];
        String content = parts.length > 1 ? parts[1] : "";

        return switch (MessageType.valueOf(type)) {
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }
}
