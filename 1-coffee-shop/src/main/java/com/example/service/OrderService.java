package com.example.service;

import com.example.dto.OrderCreateRequest;
import com.example.dto.OrderQueryRequest;
import com.example.dto.OrderResponse;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 根据ID获取订单
     */
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 根据订单ID获取订单
     */
    public Order getOrder(String orderId) {
        return orderMapper.selectByOrderId(orderId);
    }

    /**
     * 根据用户ID和订单ID获取订单
     */
    public OrderResponse getOrderByUserIdAndOrderId(Long userId, String orderId) {
        Order order = orderMapper.selectByUserIdAndOrderId(userId, orderId);
        return order != null ? new OrderResponse(order) : null;
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据用户ID获取订单列表
     */
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        return orders.stream().map(OrderResponse::new).toList();
    }

    /**
     * 根据用户ID获取订单列表（返回Entity）
     */
    public List<Order> getByUserId(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    /**
     * 根据条件查询订单
     */
    public List<OrderResponse> queryOrders(OrderQueryRequest request) {
        List<Order> orders = orderMapper.selectByConditions(
            request.getUserId(),
            request.getProductName(),
            request.getSweetness(),
            request.getIceLevel(),
            request.getStartTime() != null ? request.getStartTime().toString() : null,
            request.getEndTime() != null ? request.getEndTime().toString() : null
        );
        return orders.stream().map(OrderResponse::new).toList();
    }

    /**
     * 创建订单（使用DTO）
     */
    public OrderResponse createOrder(OrderCreateRequest request) {

        // 从数据库查询产品信息
        Product product = productMapper.selectByNameAndStatus(request.getProductName(), 1);
        if (product == null) {
            throw new IllegalArgumentException("产品不存在或已下架: " + request.getProductName());
        }

        // 检查库存
        if (product.getStock() < request.getQuantity()) {
            String errorMsg = String.format("库存不足，产品: %s, 当前库存: %d, 需要数量: %d",
                    request.getProductName(), product.getStock(), request.getQuantity());
            log.error("创建订单失败: {}", errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(request.getQuantity()));
        String orderId = "ORDER_" + System.currentTimeMillis();


        // 创建订单
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(request.getUserId());
        order.setProductId(product.getId());
        order.setProductName(request.getProductName());
        order.setSweetness(request.getSweetness());
        order.setIceLevel(request.getIceLevel());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(unitPrice);
        order.setTotalPrice(totalPrice);
        order.setRemark(request.getRemark());

        orderMapper.insert(order);

        return new OrderResponse(order);
    }

    /**
     * 创建订单（使用Entity）
     */
    public int create(Order order) {
        return orderMapper.insert(order);
    }

    /**
     * 更新订单
     */
    public int update(Order order) {
        return orderMapper.update(order);
    }

    /**
     * 更新订单备注
     */
    public OrderResponse updateOrderRemark(Long userId, String orderId, String remark) {
        Order order = orderMapper.selectByUserIdAndOrderId(userId, orderId);
        if (order == null) {
            return null;
        }
        orderMapper.updateRemark(order.getId(), remark);
        order.setRemark(remark);
        return new OrderResponse(order);
    }

    /**
     * 删除订单
     */
    public int delete(Long id) {
        return orderMapper.deleteById(id);
    }

    /**
     * 根据用户ID和订单ID删除订单
     */
    public boolean deleteOrder(Long userId, String orderId) {
        Order order = orderMapper.selectByUserIdAndOrderId(userId, orderId);
        if (order == null) {
            return false;
        }
        return orderMapper.deleteByUserIdAndOrderId(userId, orderId) > 0;
    }

    /**
     * 检查库存
     */
    public boolean checkStock(String productName, int quantity) {
        return productMapper.checkStock(productName, quantity);
    }

    /**
     * 验证产品是否存在
     */
    public boolean validateProduct(String productName) {
        return productMapper.selectByName(productName) != null;
    }
}
