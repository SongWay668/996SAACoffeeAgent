package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 咖啡店订单 Agent 启动类
 */
@SpringBootApplication
public class OrderAgentMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderAgentMain.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店订单 Agent 启动成功！");
        System.out.println("访问地址: http://localhost:8083/order.html");
        System.out.println("========================================");
    }
}
