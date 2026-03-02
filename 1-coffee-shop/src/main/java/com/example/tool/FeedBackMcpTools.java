package com.example.tool;


import com.example.entity.Feedback;
import com.example.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * feedback MCP tools
 */
@Slf4j
@Service
public class FeedBackMcpTools {

    private final FeedbackService feedbackService;

    public FeedBackMcpTools(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @McpTool(name = "feedback_create_feedback",description = "创建用户反馈记录，userId是必填项")
    public String fbCreateFeedback(
        @McpToolParam(description = "用户ID，必填")  Long userId,
         @McpToolParam(description = "反馈类型：1-产品反馈，2-服务反馈，3-投诉，4-建议") Integer feedbackType,
        @McpToolParam(description = "反馈内容") String content,
        @McpToolParam(description = "关联订单ID，可选",required = false)  String orderId,
        @McpToolParam(description = "评分1-5星,可选",required = false) Integer rating
        ){

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(feedbackType);
        feedback.setContent(content);
        if (orderId != null && !orderId.trim().isEmpty()) {
            feedback.setOrderId(orderId);
        }
        if (rating != null) {
            feedback.setRating(rating);
        }

        Feedback fb = feedbackService.create(feedback);
        return String.format("反馈记录创建成功！反馈ID: %d, 用户ID: %d, 反馈类型: %s, 内容: %s",
                fb.getId(),
                fb.getUserId(),
                fb.getFeedbackType(),
                fb.getContent());
    }

    /**
     * 根据订单ID查询反馈记录
     */
    @McpTool(name= "feedback_get_feedback_by_order", description = "根据订单ID查询反馈记录")
    public String fbGetFeedbacksByOrderId(@McpToolParam(description = "订单ID") String orderId) {
        try {
            List<Feedback> feedbacks = feedbackService.getFeedbacksByOrderId(orderId);
            if (feedbacks.isEmpty()) {
                return "该订单暂无反馈记录";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("订单 %s 的反馈记录（共 %d 条）：\n", orderId, feedbacks.size()));

            for (Feedback feedback : feedbacks) {
                result.append(String.format("- 反馈ID: %d, 用户ID: %d, 类型: %s, 评分: %s, 内容: %s, 时间: %s\n",
                        feedback.getId(),
                        feedback.getUserId(),
                        feedback.getFeedbackType(),
                        feedback.getRatingText(),
                        feedback.getContent(),
                        feedback.getCreatedAt()));
            }

            return result.toString();
        } catch (Exception e) {
            return "查询订单反馈记录失败: " + e.getMessage();
        }
    }

    /**
     * 更新反馈解决方案
     */
    @McpTool(name= "feedback_update_solution", description = "更新反馈解决方案")
    public String fbUpdateFeedbackSolution(
            @McpToolParam(description = "反馈ID") Long feedbackId,
            @McpToolParam(description = "解决方案") String solution) {
        try {
            boolean success = feedbackService.updateFeedbackSolution(feedbackId, solution);
            if (success) {
                return String.format("反馈ID %d 的解决方案更新成功：%s", feedbackId, solution);
            } else {
                return String.format("反馈ID %d 的解决方案更新失败", feedbackId);
            }
        } catch (Exception e) {
            return "更新反馈解决方案失败: " + e.getMessage();
        }
    }

}
