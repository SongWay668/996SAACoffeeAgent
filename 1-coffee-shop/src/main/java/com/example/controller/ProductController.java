package com.example.controller;

import com.example.common.Result;
import com.example.entity.Product;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("产品不存在");
        }
        return Result.success(product);
    }

    @GetMapping
    public Result<List<Product>> getAll() {
        return Result.success(productService.getAll());
    }

    @GetMapping("/available")
    public Result<List<Product>> getAvailable() {
        return Result.success(productService.getAvailable());
    }

    @PostMapping
    public Result<String> create(@RequestBody Product product) {
        int result = productService.create(product);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        int result = productService.update(product);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        int result = productService.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
