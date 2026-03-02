package com.example.controller;

import com.example.common.Result;
import com.example.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Reference 缓存管理 Controller
 */
@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    @Autowired
    private ReferenceService referenceService;

    /**
     * 刷新缓存
     */
    @PostMapping("/cache/refresh")
    public Result<String> refreshCache() {
        referenceService.refreshCache();
        return Result.success("缓存刷新成功");
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public Result<Map<String, Object>> getCacheStats() {
        return Result.success(referenceService.getCacheStats());
    }
}
