package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.mapper")
public class CoffeeShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoffeeShopApplication.class, args);
        System.out.println("========================================");
        System.out.println("咖啡店 和 MCP 启动成功！");
        System.out.println("访问地址: http://localhost:8080/index.html");
        System.out.println("========================================");
    }
}
