package com.example.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 订单查询请求 DTO
 */
public class OrderQueryRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String orderId;

    private String productName;

    private Integer sweetness;

    private Integer iceLevel;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer page = 0;

    private Integer size = 20;

    // 构造函数
    public OrderQueryRequest() {}

    public OrderQueryRequest(Long userId) {
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getSweetness() {
        return sweetness;
    }

    public void setSweetness(Integer sweetness) {
        this.sweetness = sweetness;
    }

    public Integer getIceLevel() {
        return iceLevel;
    }

    public void setIceLevel(Integer iceLevel) {
        this.iceLevel = iceLevel;
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
        return "OrderQueryRequest{" +
                "userId=" + userId +
                ", orderId='" + orderId + '\'' +
                ", productName='" + productName + '\'' +
                ", sweetness=" + sweetness +
                ", iceLevel=" + iceLevel +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
