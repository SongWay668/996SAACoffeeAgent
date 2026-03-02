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

package com.example.service;

import com.example.dto.Mem0ServerRequest;
import com.example.dto.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.config.Mem0Config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MemoryService {
    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);
    private static final String MEMORIES_URI_V1 = "/memories";
    private static final String MEMORIES_URI_V2 = "/search";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Mem0Config config;
    private final ApplicationContext applicationContext;

    @Autowired
    public MemoryService(RestTemplate restTemplate, Mem0Config config, ApplicationContext applicationContext) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.config = config;
        this.applicationContext = applicationContext;
    }

    public String searchMemory(String userId, String query) {
        try {
            // 构建请求体 - 本地 mem0 server 的搜索格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("user_id", userId);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            logger.info("Sending memory search request: {}", requestJson);

            // 使用 RestTemplate 进行同步调用，避免在 reactive 上下文中使用 block()
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 本地 server 不需要 Token 认证
            // 直接传递 requestBody 对象，而不是 requestJson 字符串
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String url = config.getApi().getUrl() + MEMORIES_URI_V2;
            logger.info("Request URL: {}", url);
            logger.info("Request headers: {}", headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            String response = responseEntity.getBody();

            logger.info("Response status: {}", responseEntity.getStatusCode());
            logger.info("Response body: {}", response);

            // 解析响应 - 本地 server 返回的是数组格式
//            List<Map<String, Object>> memories = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});

            Map<String, Object> rawMap = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>(){}
            );
            List<Map<String, Object>> memories = new  ArrayList<>();
            if(null != rawMap && rawMap.get("results") instanceof List) {
                memories = (List<Map<String, Object>>) rawMap.get("results");
            }

            if (!memories.isEmpty()) {
                StringBuilder result = new StringBuilder();

                for (int i = 0; i < memories.size(); i++) {
                    Map<String, Object> memory = memories.get(i);
                    result.append(memory.get("memory"));

                    // 如果不是最后一个记忆，添加换行符
                    if (i < memories.size() - 1) {
                        result.append("\n");
                    }
                }
                logger.info("Found {} memories for user: {}", memories.size(), userId);
                return result.toString();
            } else {
                logger.warn("No memories found for user: {}", userId);
                return "未找到用户历史喜好";
            }
        } catch (Exception e) {
            logger.error("Error searching memories for user: {}", userId, e);
            return "未找到用户历史喜好";
        }
    }

    /**
     * 存储用户记忆 - 异步方法，立即返回成功状态
     */
    public String storeMemory(String userId, String content) {
        // 立即返回成功状态
        logger.info("Memory storage request received for user: {}, content: {}", userId, content);
        
        // 通过ApplicationContext获取代理对象来调用异步方法
        MemoryService self = applicationContext.getBean(MemoryService.class);
        self.storeMemoryAsync(userId, content);

        return "成功存储用户喜好";
    }
    
    /**
     * 异步存储用户记忆 - 后台执行
     */
    @Async("memoryTaskExecutor")
    public void storeMemoryAsync(String userId, String content) {
        try {
            logger.info("Starting async memory storage for user: {}", userId);

            // 使用本地 mem0 server 的格式
            Map<String, Object> requestBody = new HashMap<>();
            // 构造消息列表格式
            List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", content)
            );
            requestBody.put("messages", messages);

            // 使用配置中的默认用户，如果未提供 userId
            String finalUserId;
            if (userId != null && !userId.trim().isEmpty()) {
                finalUserId = userId;
            } else {
                // 从配置获取默认用户，如果没有则使用 mem0 配置中的用户
                finalUserId = "ws16289"; // 与 mem0/openmemory/api/.env 中的 USER 一致
            }
            requestBody.put("user_id", finalUserId);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            logger.info("Sending async memory request: {}", requestJson);

            // 使用 RestTemplate 进行同步调用，避免在异步方法中使用阻塞操作
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 本地 server 不需要 Token 认证
            // 直接传递 requestBody 对象，而不是 requestJson 字符串
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String url = config.getApi().getUrl() + MEMORIES_URI_V1;
            logger.info("Request URL: {}", url);
            logger.info("Request headers: {}", headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            String response = responseEntity.getBody();

            logger.info("Response status code: {}", responseEntity.getStatusCodeValue());
            logger.info("Response status: {}", responseEntity.getStatusCode());
            logger.info("Response headers: {}", responseEntity.getHeaders());
            logger.info("Response body: {}", response);

            if (response != null) {
                logger.info("Successfully added memory for user: {}", finalUserId);
                logger.info("Memory creation response: {}", response);
            }

            logger.info("Async memory storage completed successfully for user: {}", finalUserId);
        } catch (Exception e) {
            logger.error("Error in async memory storage for user: {}", userId, e);
        }
    }
}