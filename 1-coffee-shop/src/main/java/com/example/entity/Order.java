package com.example.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderId;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer sweetness;
    private String sweetnessName;  // 甜度显示名称
    private Integer iceLevel;
    private String iceLevelName;   // 冰度显示名称
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
