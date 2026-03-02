package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer shelfTime;
    private Integer preparationTime;
    private Integer isSeasonal;
    private LocalDate seasonStart;
    private LocalDate seasonEnd;
    private Integer isRegional;
    private String availableRegions;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
