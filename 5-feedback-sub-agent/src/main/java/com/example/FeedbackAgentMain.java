package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 咖啡店反馈 Agent 启动类
 */
@SpringBootApplication
public class FeedbackAgentMain {
    public static void main(String[] args) {
        SpringApplication.run(FeedbackAgentMain.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店反馈 Agent 启动成功！");
        System.out.println("访问地址: http://localhost:8084/feedback.html");
        System.out.println("========================================");
    }
}
