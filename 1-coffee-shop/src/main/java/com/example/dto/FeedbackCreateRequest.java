package com.example.dto;

import jakarta.validation.constraints.*;

/**
 * 创建反馈请求 DTO
 */
public class FeedbackCreateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String orderId;

    @NotNull(message = "反馈类型不能为空")
    @Min(value = 1, message = "反馈类型必须在1-4之间")
    @Max(value = 4, message = "反馈类型必须在1-4之间")
    private Integer feedbackType;

    @Min(value = 1, message = "评分必须在1-5之间")
    @Max(value = 5, message = "评分必须在1-5之间")
    private Integer rating;

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    // 构造函数
    public FeedbackCreateRequest() {}

    public FeedbackCreateRequest(Long userId, Integer feedbackType, String content) {
        this.userId = userId;
        this.feedbackType = feedbackType;
        this.content = content;
    }

    public FeedbackCreateRequest(String orderId, Long userId, Integer feedbackType,
                            Integer rating, String content) {
        this.userId = userId;
        this.orderId = orderId;
        this.feedbackType = feedbackType;
        this.rating = rating;
        this.content = content;
    }

    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(Integer feedbackType) {
        this.feedbackType = feedbackType;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FeedbackCreateRequest{" +
                "userId=" + userId +
                ", orderId='" + orderId + '\'' +
                ", feedbackType=" + feedbackType +
                ", rating=" + rating +
                ", content='" + content + '\'' +
                '}';
    }
}
