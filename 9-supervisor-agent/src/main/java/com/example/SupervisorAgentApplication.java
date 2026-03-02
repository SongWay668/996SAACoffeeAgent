package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SupervisorAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupervisorAgentApplication.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店反馈 Supervisor 启动成功！");
        System.out.println("访问地址: http://localhost:10008/index.html");
        System.out.println("========================================");
    }
}
