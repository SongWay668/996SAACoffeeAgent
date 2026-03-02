package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
    }
)
public class MemoryMcpMain {
    public static void main(String[] args) {
        SpringApplication.run(MemoryMcpMain.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店记忆 Agent 启动成功！");
        System.out.println("URL http://localhost:8081");
        System.out.println("========================================");
    }
}