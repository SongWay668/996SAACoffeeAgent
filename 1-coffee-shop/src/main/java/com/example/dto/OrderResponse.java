package com.example.dto;

import com.example.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应 DTO
 */
public class OrderResponse {

    private Long id;
    private String orderId;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer sweetness;
    private String sweetnessName;
    private Integer iceLevel;
    private String iceLevelName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public OrderResponse() {}

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.orderId = order.getOrderId();
        this.userId = order.getUserId();
        this.productId = order.getProductId();
        this.productName = order.getProductName();
        this.sweetness = order.getSweetness();
        this.sweetnessName = order.getSweetnessName();
        this.iceLevel = order.getIceLevel();
        this.iceLevelName = order.getIceLevelName();
        this.quantity = order.getQuantity();
        this.unitPrice = order.getUnitPrice();
        this.totalPrice = order.getTotalPrice();
        this.remark = order.getRemark();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getSweetnessName() {
        return sweetnessName;
    }

    public void setSweetnessName(String sweetnessName) {
        this.sweetnessName = sweetnessName;
    }

    public Integer getIceLevel() {
        return iceLevel;
    }

    public void setIceLevel(Integer iceLevel) {
        this.iceLevel = iceLevel;
    }

    public String getIceLevelName() {
        return iceLevelName;
    }

    public void setIceLevelName(String iceLevelName) {
        this.iceLevelName = iceLevelName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
        return "OrderResponse{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", sweetness=" + sweetness +
                ", sweetnessName='" + sweetnessName + '\'' +
                ", iceLevel=" + iceLevel +
                ", iceLevelName='" + iceLevelName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", remark='" + remark + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
