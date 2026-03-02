package com.example.controller;

import com.example.common.Result;
import com.example.entity.Order;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping
    public Result<List<Order>> getAll() {
        return Result.success(orderService.getAll());
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getByUserId(@PathVariable Long userId) {
        return Result.success(orderService.getByUserId(userId));
    }

    @PostMapping
    public Result<String> create(@RequestBody Order order) {
        int result = orderService.create(order);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        int result = orderService.update(order);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        int result = orderService.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
