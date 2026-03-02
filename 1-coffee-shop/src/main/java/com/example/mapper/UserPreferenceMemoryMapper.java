package com.example.mapper;

import com.example.entity.UserPreferenceMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserPreferenceMemoryMapper {
    UserPreferenceMemory selectById(@Param("id") Long id);
    List<UserPreferenceMemory> selectByUserId(@Param("userId") Long userId);
    List<UserPreferenceMemory> selectAll();
    int insert(UserPreferenceMemory memory);
    int deleteById(@Param("id") Long id);
    int deleteByUserId(@Param("userId") Long userId);
}
