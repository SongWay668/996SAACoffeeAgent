package com.example.tool;

import com.example.cache.ReferenceCache;
import com.example.dto.OrderCreateRequest;
import com.example.dto.OrderQueryRequest;
import com.example.dto.OrderResponse;
import com.example.entity.Order;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CoffeeOrderMcpTools {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReferenceCache referenceCache;
    /**
     * 创建订单工具（新接口，支持用户ID）
     */
    @McpTool(name = "order_create_order_with_user", description = "为用户创建新的咖啡订单。支持海风咖啡店的所有产品，包括焦糖玛奇朵,冷萃咖啡,香草拿铁,港式丝袜咖啡等经典产品。系统会自动检查库存并计算价格。")
    public String order_createOrderWithUser(
            @McpToolParam(description = "用户ID，必须为正整数") Long userId,
            @McpToolParam(description = "产品名称，必须是海风咖啡店的现有产品，如：焦糖玛奇朵,冷萃咖啡,香草拿铁,港式丝袜咖啡") String productName,
            @McpToolParam(description = "甜度要求，可选值：标准糖、少糖、半糖、微糖、无糖") String sweetness,
            @McpToolParam(description = "冰量要求，可选值：正常冰、少冰、去冰、温、热") String iceLevel,
            @McpToolParam(description = "购买数量，必须为正整数，默认为1") int quantity,
            @McpToolParam(description = "订单备注，可选") String remark) {
        try {
            // 转换甜度为数字（从Reference缓存获取）
            Integer sweetnessLevel = convertSweetnessToNumber(sweetness);

            // 转换冰量为数字（从Reference缓存获取）
            Integer iceLevelNumber = convertIceLevelToNumber(iceLevel);

            OrderCreateRequest request = new OrderCreateRequest(userId, null, productName,
                    sweetnessLevel, iceLevelNumber, quantity, remark);

            OrderResponse order = orderService.createOrder(request);
            return String.format("订单创建成功！订单ID: %s, 用户ID: %d, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元",
                    order.getOrderId(), order.getUserId(), order.getProductName(),
                    order.getSweetnessName(), order.getIceLevelName(), order.getQuantity(), order.getTotalPrice());
        } catch (Exception e) {
            return "创建订单失败: " + e.getMessage();
        }
    }

    /**
     * 将甜度文本转换为数字（从Reference缓存）
     */
    private Integer convertSweetnessToNumber(String sweetnessText) {
        if (sweetnessText == null || sweetnessText.trim().isEmpty()) {
            return 3; // 默认半糖
        }

        // 从Reference缓存获取所有甜度选项
        Map<Integer, String> sweetnessMap = referenceCache.getRefTypeData("sweetness");

        // 反向查找：根据refValue匹配refCode
        for (Map.Entry<Integer, String> entry : sweetnessMap.entrySet()) {
            if (entry.getValue().equals(sweetnessText.trim())) {
                return entry.getKey();
            }
        }

        // 未找到时返回默认值
        return 3;
    }

    /**
     * 将冰量文本转换为数字（从Reference缓存）
     */
    private Integer convertIceLevelToNumber(String iceLevelText) {
        if (iceLevelText == null || iceLevelText.trim().isEmpty()) {
            return 5; // 默认正常冰
        }

        // 从Reference缓存获取所有冰量选项
        Map<Integer, String> iceLevelMap = referenceCache.getRefTypeData("ice_level");

        // 反向查找：根据refValue匹配refCode
        for (Map.Entry<Integer, String> entry : iceLevelMap.entrySet()) {
            if (entry.getValue().equals(iceLevelText.trim())) {
                return entry.getKey();
            }
        }

        // 未找到时返回默认值
        return 5;
    }

    /**
     * 查询订单工具（兼容原有接口）
     */
    @McpTool(name = "order_get_order", description = "根据订单ID查询订单的详细信息，包括产品名称、甜度、冰量、数量、价格和创建时间等完整信息。")
    public String order_getOrder(@McpToolParam(description = "订单ID，格式为ORDER_开头的唯一标识符，例如：ORDER_1693654321000") String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            if (order == null) {
                return "订单不存在: " + orderId;
            }

            return String.format("订单信息 - ID: %s, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元, 创建时间: %s",
                    order.getOrderId(), order.getProductName(), order.getSweetnessName(),
                    order.getIceLevelName(), order.getQuantity(), order.getTotalPrice(),
                    order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            return "查询订单失败: " + e.getMessage();
        }
    }

    /**
     * 根据用户ID和订单ID查询订单工具
     */
    @McpTool(name = "order_get_order_by_user", description = "根据用户ID和订单ID查询订单的详细信息，包括产品名称、甜度、冰量、数量、价格和创建时间等完整信息。")
    public String order_getOrderByUser(
            @McpToolParam(description = "用户ID，必须为正整数") Long userId,
            @McpToolParam(description = "订单ID，格式为ORDER_开头的唯一标识符，例如：ORDER_1693654321000") String orderId) {
        try {
            OrderResponse order = orderService.getOrderByUserIdAndOrderId(userId, orderId);
            if (order == null) {
                return "订单不存在: " + orderId + " (用户ID: " + userId + ")";
            }

            return String.format("订单信息 - ID: %s, 用户ID: %d, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元, 创建时间: %s",
                    order.getOrderId(), order.getUserId(), order.getProductName(),
                    order.getSweetnessName(), order.getIceLevelName(), order.getQuantity(),
                    order.getTotalPrice(), order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
            return "查询订单失败: " + e.getMessage();
        }
    }

    /**
     * 检查库存工具
     */
    @McpTool(name = "order_check_stock", description = "检查指定产品的库存是否充足，确保在下单前能够满足用户的需求数量。返回库存状态和可用性信息。")
    public String order_checkStock(
            @McpToolParam(description = "产品名称，必须是云边奶茶铺的现有产品，如：云边茉莉、桂花云露、云雾观音、云山红韵、云桃乌龙、云边普洱、云桂龙井、云峰山茶") String productName,
            @McpToolParam(description = "需要检查的数量，必须为正整数，表示用户想要购买的数量") int quantity) {
        try {
            boolean available = orderService.checkStock(productName, quantity);
            return available ?
                    String.format("产品 %s 库存充足，可提供 %d 件", productName, quantity) :
                    String.format("产品 %s 库存不足，无法提供 %d 件", productName, quantity);
        } catch (Exception e) {
            return "检查库存失败: " + e.getMessage();
        }
    }

    /**
     * 获取所有订单工具（兼容原有接口）
     */
    @McpTool(name = "order_get_orders", description = "获取系统中所有订单的列表，包括订单ID、产品信息、价格和创建时间。用于查看订单历史和统计信息。")
    public String order_getAllOrders() {
        try {
            List<Order> orders = orderService.getAll();
            if (orders.isEmpty()) {
                return "当前没有任何订单记录。";
            }

            StringBuilder result = new StringBuilder("所有订单列表:\n");
            for (Order order : orders) {
                result.append(String.format("- 订单ID: %s, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元, 创建时间: %s\n",
                        order.getOrderId(), order.getProductName(), order.getSweetnessName(),
                        order.getIceLevelName(), order.getQuantity(), order.getTotalPrice(),
                        order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }

            return result.toString();
        } catch (Exception e) {
            return "获取订单列表失败: " + e.getMessage();
        }
    }

    /**
     * 根据用户ID获取订单列表工具
     */
    @McpTool(name = "order_get_orders_by_user", description = "根据用户ID获取该用户的所有订单列表，包括订单ID、产品信息、价格和创建时间。用于查看用户的订单历史。")
    public String order_getOrdersByUser(@McpToolParam(description = "用户ID，必须为正整数") Long userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            if (orders.isEmpty()) {
                return "用户 " + userId + " 当前没有任何订单记录。";
            }

            StringBuilder result = new StringBuilder("用户 " + userId + " 的订单列表:\n");
            for (OrderResponse order : orders) {
                result.append(String.format("- 订单ID: %s, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元, 创建时间: %s\n",
                        order.getOrderId(), order.getProductName(), order.getSweetnessName(),
                        order.getIceLevelName(), order.getQuantity(), order.getTotalPrice(),
                        order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }

            return result.toString();
        } catch (Exception e) {
            return "获取用户订单列表失败: " + e.getMessage();
        }
    }

    /**
     * 多维度查询用户订单工具
     */
    @McpTool(name = "order_query_orders", description = "根据多个条件查询用户订单，支持按产品名称、甜度、冰量、时间范围等条件筛选。")
    public String order_queryOrders(
            @McpToolParam(description = "用户ID，必须为正整数") Long userId,
            @McpToolParam(description = "产品名称，可选，支持模糊匹配") String productName,
            @McpToolParam(description = "甜度，可选，1-无糖，2-微糖，3-半糖，4-少糖，5-标准糖") Integer sweetness,
            @McpToolParam(description = "冰量，可选，1-热，2-温，3-去冰，4-少冰，5-正常冰") Integer iceLevel,
            @McpToolParam(description = "开始时间，可选，格式：yyyy-MM-dd HH:mm:ss") String startTime,
            @McpToolParam(description = "结束时间，可选，格式：yyyy-MM-dd HH:mm:ss") String endTime) {
        try {
            OrderQueryRequest request = new OrderQueryRequest(userId);
            request.setProductName(productName);
            request.setSweetness(sweetness);
            request.setIceLevel(iceLevel);

            if (startTime != null && !startTime.trim().isEmpty()) {
                request.setStartTime(LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                request.setEndTime(LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            List<OrderResponse> orders = orderService.queryOrders(request);
            if (orders.isEmpty()) {
                return "未找到符合条件的订单记录。";
            }

            StringBuilder result = new StringBuilder("查询结果 (" + orders.size() + " 条记录):\n");
            for (OrderResponse order : orders) {
                result.append(String.format("- 订单ID: %s, 产品: %s, 甜度: %s, 冰量: %s, 数量: %d, 价格: %.2f元, 创建时间: %s\n",
                        order.getOrderId(), order.getProductName(), order.getSweetnessName(),
                        order.getIceLevelName(), order.getQuantity(), order.getTotalPrice(),
                        order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }

            return result.toString();
        } catch (Exception e) {
            return "查询订单失败: " + e.getMessage();
        }
    }

    /**
     * 删除订单工具
     */
    @McpTool(name = "order_delete_order", description = "根据用户ID和订单ID删除订单。只能删除属于该用户的订单。")
    public String order_deleteOrder(
            @McpToolParam(description = "用户ID，必须为正整数") Long userId,
            @McpToolParam(description = "订单ID，格式为ORDER_开头的唯一标识符") String orderId) {
        try {
            boolean deleted = orderService.deleteOrder(userId, orderId);
            if (deleted) {
                return "订单删除成功: " + orderId;
            } else {
                return "订单删除失败，订单不存在或无权限: " + orderId;
            }
        } catch (Exception e) {
            return "删除订单失败: " + e.getMessage();
        }
    }

    /**
     * 更新订单备注工具
     */
    @McpTool(name = "order_update_remark", description = "根据用户ID和订单ID更新订单备注。只能更新属于该用户的订单。")
    public String order_updateOrderRemark(
            @McpToolParam(description = "用户ID，必须为正整数") Long userId,
            @McpToolParam(description = "订单ID，格式为ORDER_开头的唯一标识符") String orderId,
            @McpToolParam(description = "新的备注内容") String remark) {
        try {
            OrderResponse order = orderService.updateOrderRemark(userId, orderId, remark);
            if (order != null) {
                return "订单备注更新成功: " + orderId + ", 新备注: " + remark;
            } else {
                return "订单备注更新失败，订单不存在或无权限: " + orderId;
            }
        } catch (Exception e) {
            return "更新订单备注失败: " + e.getMessage();
        }
    }

    /**
     * 验证产品是否存在工具
     */
    @McpTool(name = "order_validate_product", description = "验证指定产品是否存在且可用。")
    public String order_validateProduct(@McpToolParam(description = "产品名称") String productName) {
        try {
            boolean exists = orderService.validateProduct(productName);
            return exists ?
                    String.format("产品 %s 存在且可用", productName) :
                    String.format("产品 %s 不存在或已下架", productName);
        } catch (Exception e) {
            return "验证产品失败: " + e.getMessage();
        }
    }
}
