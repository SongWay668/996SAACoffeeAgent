package com.example.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.service.FeedbackAgentService;
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
 * 反馈 Controller
 *
 * 使用 FeedbackAgentService 实现基于 ReactAgent 的流式对话
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackAgentService feedbackAgentService;

    /**
     * 流式聊天（Streamable-HTTP）
     * 直接返回文本流，不使用 SSE 格式
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamFeedback(
            @RequestParam String conversation_id,
            @RequestParam String user_input,
            @RequestParam String user_id) {

        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("收到请求（流式） - requestId: {}, conversation_id: {}, user_id: {}, query: {}", requestId, conversation_id, user_id, user_input);

        try {
            return feedbackAgentService.streamFeedback(conversation_id, user_input, user_id)
                    .doOnError(e -> logger.error("流式处理失败 - conversation_id: {}", conversation_id, e));
        } catch (GraphRunnerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空会话
     */
    @GetMapping("/clear")
    public String clear(@RequestParam String conversation_id) {
        logger.info("清空会话 - conversation_id: {}", conversation_id);
        // 无状态会话，无需清空
        return "会话已清空: " + conversation_id;
    }
}
