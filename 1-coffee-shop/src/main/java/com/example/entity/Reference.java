package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reference {
    private Long id;
    private String refType;
    private String refKey;
    private String refValue;
    private Integer refCode;
    private Integer sortOrder;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
