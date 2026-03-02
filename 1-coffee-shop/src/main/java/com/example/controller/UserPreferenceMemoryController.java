package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserPreferenceMemory;
import com.example.service.UserPreferenceMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user-preference-memory")
public class UserPreferenceMemoryController {

    @Autowired
    private UserPreferenceMemoryService userPreferenceMemoryService;

    @GetMapping("/{id}")
    public Result<UserPreferenceMemory> getById(@PathVariable Long id) {
        return Result.success(userPreferenceMemoryService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public Result<List<UserPreferenceMemory>> getByUserId(@PathVariable Long userId) {
        return Result.success(userPreferenceMemoryService.getByUserId(userId));
    }

    @GetMapping
    public Result<List<UserPreferenceMemory>> getAll() {
        return Result.success(userPreferenceMemoryService.getAll());
    }

    @PostMapping
    public Result<Void> create(@RequestBody UserPreferenceMemory memory) {
        userPreferenceMemoryService.create(memory);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userPreferenceMemoryService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}")
    public Result<Void> deleteByUserId(@PathVariable Long userId) {
        userPreferenceMemoryService.deleteByUserId(userId);
        return Result.success();
    }
}
