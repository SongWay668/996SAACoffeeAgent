package com.example.dto;

import com.example.entity.Feedback;

import java.time.LocalDateTime;

/**
 * 反馈响应 DTO
 */
public class FeedbackResponse {

    private Long id;
    private String orderId;
    private Long userId;
    private Integer feedbackType;
    private String feedbackTypeDesc;
    private Integer rating;
    private String ratingDesc;
    private String content;
    private String solution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public FeedbackResponse() {}

    public FeedbackResponse(Feedback feedback) {
        this.id = feedback.getId();
        this.orderId = feedback.getOrderId();
        this.userId = feedback.getUserId();
        this.feedbackType = feedback.getFeedbackType();
        this.feedbackTypeDesc = feedback.getFeedbackTypeDesc();
        this.rating = feedback.getRating();
        this.ratingDesc = feedback.getRatingDesc();
        this.content = feedback.getContent();
        this.solution = feedback.getSolution();
        this.createdAt = feedback.getCreatedAt();
        this.updatedAt = feedback.getUpdatedAt();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(Integer feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getFeedbackTypeDesc() {
        return feedbackTypeDesc;
    }

    public void setFeedbackTypeDesc(String feedbackTypeDesc) {
        this.feedbackTypeDesc = feedbackTypeDesc;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingDesc() {
        return ratingDesc;
    }

    public void setRatingDesc(String ratingDesc) {
        this.ratingDesc = ratingDesc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FeedbackResponse{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", feedbackType=" + feedbackType +
                ", feedbackTypeDesc='" + feedbackTypeDesc + '\'' +
                ", rating=" + rating +
                ", ratingDesc='" + ratingDesc + '\'' +
                ", content='" + content + '\'' +
                ", solution='" + solution + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
