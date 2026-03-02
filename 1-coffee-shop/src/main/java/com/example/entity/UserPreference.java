package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPreference {
    private Long id;
    private Long userId;
    private Integer preferredSweetness;
    private Integer preferredIceLevel;
    private String preferredDrinkType;
    private Integer preferredStrength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
