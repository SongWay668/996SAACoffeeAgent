package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;
    private String orderId;
    private Long userId;
    private Integer feedbackType;
    private Integer rating;
    private String content;
    private String solution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 获取反馈类型对应的描述（从 reference 表查询）
     * 需要在外部通过 FeedbackService 设置
     */
    private String feedbackTypeDesc;

    /**
     * 获取评分对应的描述（从 reference 表查询）
     * 需要在外部通过 FeedbackService 设置
     */
    private String ratingDesc;

    /**
     * 获取评分描述（已废弃，使用 ratingDesc）
     * @return 评分描述（如 "5星"）
     */
    @Deprecated
    public String getRatingText() {
        if (ratingDesc != null) {
            return ratingDesc;
        }
        if (rating == null) {
            return "未评分";
        }
        return rating + "星";
    }
}
