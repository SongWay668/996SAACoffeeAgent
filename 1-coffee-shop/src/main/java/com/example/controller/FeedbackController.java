package com.example.controller;

import com.example.common.Result;
import com.example.entity.Feedback;
import com.example.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/{id}")
    public Result<Feedback> getById(@PathVariable Long id) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.success(feedback);
    }

    @GetMapping
    public Result<List<Feedback>> getAll() {
        return Result.success(feedbackService.getAll());
    }

    @GetMapping("/user/{userId}")
    public Result<List<Feedback>> getByUserId(@PathVariable Long userId) {
        return Result.success(feedbackService.getByUserId(userId));
    }

    @PostMapping
    public Result<Feedback> create(@RequestBody Feedback feedback) {
        Feedback created = feedbackService.create(feedback);
        if (created != null) {
            // 填充反馈类型描述
            feedbackService.fillFeedbackTypeDesc(created);
            return Result.success(created);
        }
        return Result.error("创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Feedback feedback) {
        feedback.setId(id);
        int result = feedbackService.update(feedback);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        int result = feedbackService.delete(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
