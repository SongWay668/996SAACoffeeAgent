package com.example.mapper;

import com.example.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserPreferenceMapper {
    UserPreference selectByUserId(@Param("userId") Long userId);
    List<UserPreference> selectAll();
    int insert(UserPreference preference);
    int update(UserPreference preference);
    int deleteByUserId(@Param("userId") Long userId);
}
