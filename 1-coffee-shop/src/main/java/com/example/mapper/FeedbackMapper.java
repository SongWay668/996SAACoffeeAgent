package com.example.mapper;

import com.example.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FeedbackMapper {
    Feedback selectById(@Param("id") Long id);
    List<Feedback> selectAll();
    List<Feedback> selectByUserId(@Param("userId") Long userId);
    List<Feedback> selectByOrderId(@Param("orderId") String orderId);
    int insert(Feedback feedback);
    int update(Feedback feedback);
    int updateSolution(@Param("id") Long id, @Param("solution") String solution);
    int deleteById(@Param("id") Long id);
}
