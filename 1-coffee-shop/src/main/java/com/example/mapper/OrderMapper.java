package com.example.mapper;

import com.example.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    Order selectById(@Param("id") Long id);
    Order selectByOrderId(@Param("orderId") String orderId);
    Order selectByUserIdAndOrderId(@Param("userId") Long userId, @Param("orderId") String orderId);
    List<Order> selectAll();
    List<Order> selectByUserId(@Param("userId") Long userId);
    List<Order> selectByConditions(@Param("userId") Long userId,
                                  @Param("productName") String productName,
                                  @Param("sweetness") Integer sweetness,
                                  @Param("iceLevel") Integer iceLevel,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);
    int insert(Order order);
    int update(Order order);
    int updateRemark(@Param("id") Long id, @Param("remark") String remark);
    int deleteById(@Param("id") Long id);
    int deleteByUserIdAndOrderId(@Param("userId") Long userId, @Param("orderId") String orderId);
}
