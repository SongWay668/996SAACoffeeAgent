package com.example.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 反馈查询请求 DTO
 */
public class FeedbackQueryRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String orderId;

    private Integer feedbackType;

    private Integer rating;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer page = 0;

    private Integer size = 20;

    // 构造函数
    public FeedbackQueryRequest() {}

    public FeedbackQueryRequest(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FeedbackQueryRequest{" +
                "userId=" + userId +
                ", orderId='" + orderId + '\'' +
                ", feedbackType=" + feedbackType +
                ", rating=" + rating +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
