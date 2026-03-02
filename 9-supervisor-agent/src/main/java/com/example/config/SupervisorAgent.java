/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.config;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
//import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
//import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

@Configuration
public class SupervisorAgent {
    private static final Logger logger = LoggerFactory.getLogger(SupervisorAgent.class);

    @Bean
    public LlmRoutingAgent supervisorAgentBean(ChatModel chatModel,
                                               @Value("classpath:SupervisorAgentPrompt.txt") Resource customerResource,
                                               @Qualifier("consultSubAgentBean") ReactAgent consultAgentBean,
                                               @Qualifier("orderSubAgentBean") ReactAgent orderAgentBean,
                                               @Qualifier("feedbackSubAgentBean") ReactAgent feedbackAgentBean
    ) throws Exception {
//        logger.info("agent card provider: {}", agentCardProvider);
        //自动状态管理 - 不再需要手动配置 KeyStrategyFactory
//        KeyStrategyFactory stateFactory = () -> {
//            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
//            keyStrategyHashMap.put("input", new ReplaceStrategy());
//            keyStrategyHashMap.put("chat_id", new ReplaceStrategy());
//            keyStrategyHashMap.put("user_id", new ReplaceStrategy());
//            keyStrategyHashMap.put("messages", new ReplaceStrategy());
//            return keyStrategyHashMap;
//        };
//        输入	"input"	用户的查询内容
//        输出	"messages"	Agent 的响应消息列表
//        会话ID	"chat_id"	用于保持对话上下文
//        用户ID	"user_id"	用于识别用户身份
        try {
            String prompt = customerResource.getContentAsString(StandardCharsets.UTF_8);
            logger.info("=== SupervisorAgent Prompt ===");
            logger.info(prompt);
            logger.info("=== End of Prompt ===");

            return LlmRoutingAgent.builder()
                    .name("supervisor_agent")
                    .model(chatModel)
//                    .state(stateFactory) //自动状态管理 - 不再需要手动配置 KeyStrategyFactory
                    .instruction(prompt)  // 使用 instruction 作为 prompt
//                    .inputKey("input")//自动使用默认的 input 和 output 键
//                    .outputKey("messages")
                    .subAgents(List.of(consultAgentBean, orderAgentBean, feedbackAgentBean))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to create LlmRoutingAgent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize supervisor agent", e);
        }
    }
}
