package com.example.service;

import com.example.entity.Feedback;
import com.example.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private ReferenceService referenceService;

    /**
     * 根据ID获取反馈
     * Mapper 已通过联合查询自动填充描述字段
     */
    public Feedback getById(Long id) {
        return feedbackMapper.selectById(id);
    }

    /**
     * 获取所有反馈
     * Mapper 已通过联合查询自动填充描述字段
     */
    public List<Feedback> getAll() {
        return feedbackMapper.selectAll();
    }

    /**
     * 根据用户ID获取反馈
     * Mapper 已通过联合查询自动填充描述字段
     */
    public List<Feedback> getByUserId(Long userId) {
        return feedbackMapper.selectByUserId(userId);
    }

    /**
     * 根据订单ID获取反馈
     * Mapper 已通过联合查询自动填充描述字段
     */
    public List<Feedback> getFeedbacksByOrderId(String orderId) {
        return feedbackMapper.selectByOrderId(orderId);
    }

    /**
     * 创建反馈
     */
    public Feedback create(Feedback feedback) {
        feedbackMapper.insert(feedback);
        // 创建后需要手动填充描述（因为刚创建的数据没有通过联合查询）
        fillFeedbackTypeDesc(feedback);
        fillRatingDesc(feedback);
        return feedback;
    }

    /**
     * 更新反馈
     */
    public int update(Feedback feedback) {
        return feedbackMapper.update(feedback);
    }

    /**
     * 更新反馈解决方案
     */
    public boolean updateFeedbackSolution(Long feedbackId, String solution) {
        return feedbackMapper.updateSolution(feedbackId, solution) > 0;
    }

    /**
     * 删除反馈
     */
    public int delete(Long id) {
        return feedbackMapper.deleteById(id);
    }

    /**
     * 填充反馈类型描述（从缓存获取）
     */
    public void fillFeedbackTypeDesc(Feedback feedback) {
        if (feedback.getFeedbackType() != null) {
            String desc = referenceService.getRefValue("feedback_type", feedback.getFeedbackType());
            feedback.setFeedbackTypeDesc(desc);
        }
    }

    /**
     * 填充评分描述（从缓存获取）
     */
    public void fillRatingDesc(Feedback feedback) {
        if (feedback.getRating() != null) {
            String desc = referenceService.getRefValue("rating", feedback.getRating());
            feedback.setRatingDesc(desc);
        }
    }
}
