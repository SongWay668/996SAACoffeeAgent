package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(@Param("id") Long id);
    List<User> selectAll();
    int insert(User user);
    int update(User user);
    int deleteById(@Param("id") Long id);

    User getById(@Param("id") Long id);
    User getByUsername(@Param("username") String username);
    List<User> getAll();
}
