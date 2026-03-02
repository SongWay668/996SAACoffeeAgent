package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsultApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultApplication.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店咨询 Agent 启动成功！");
        System.out.println("访问地址: http://localhost:8082/chat.html");
        System.out.println("========================================");
    }
}
