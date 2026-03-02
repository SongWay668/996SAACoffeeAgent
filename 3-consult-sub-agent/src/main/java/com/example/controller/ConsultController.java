package com.example.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.service.ConsultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 咨询 Controller
 *
 * 使用 ConsultService 实现基于 ReactAgent 的流式对话
 */
@RestController
@RequestMapping("/api/chat")
public class ConsultController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultController.class);

    @Autowired
    private ConsultService consultService;

    /**
     * 流式聊天（Streamable-HTTP）
     * 直接返回文本流，不使用 SSE 格式
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(
            @RequestParam String chat_id,
            @RequestParam String user_query,
            @RequestParam(required = false) String user_id) {

        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("收到请求（流式） - requestId: {}, chat_id: {}, user_id: {}, query: {}", requestId, chat_id, user_id, user_query);

        try {
            return consultService.streamConsult(chat_id, user_query, user_id)
                    .doOnError(e -> logger.error("流式处理失败 - chat_id: {}", chat_id, e));
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空会话
     */
    @GetMapping("/clear")
    public String clear(@RequestParam String chat_id) {
        logger.info("清空会话 - chat_id: {}", chat_id);
        consultService.clearConversation(chat_id);
        return "会话已清空: " + chat_id;
    }
}
