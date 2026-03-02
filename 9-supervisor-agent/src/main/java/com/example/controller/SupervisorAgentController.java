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
 * 使用 Agent Tool 模式：主 Agent 将子 Agent 作为工具调用
 */
@RestController
@RequestMapping("/api/supervisor")
public class SupervisorAgentController {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorAgentController.class);
    private final LlmRoutingAgent supervisorAgent;

    public SupervisorAgentController(
            @Qualifier("supervisorAgentBean") LlmRoutingAgent supervisorAgent) {
        this.supervisorAgent = supervisorAgent;
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
            // 构建输入 - Agent Tool 模式下，直接传递 user query 即可
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
            input.put("chat_id", chat_id);
            input.put("conversation_id", chat_id);

            logger.info("调用 supervisorAgent.stream() - requestId: {}", requestId);
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

}
