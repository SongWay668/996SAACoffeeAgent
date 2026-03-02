package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPreferenceMemory {
    private Long id;
    private Long userId;
    private String preferenceContent;
    private LocalDateTime createdAt;
}
