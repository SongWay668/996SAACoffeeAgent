package com.example.service;

import com.example.entity.UserPreferenceMemory;
import com.example.mapper.UserPreferenceMemoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserPreferenceMemoryService {

    @Autowired
    private UserPreferenceMemoryMapper userPreferenceMemoryMapper;

    public UserPreferenceMemory getById(Long id) {
        return userPreferenceMemoryMapper.selectById(id);
    }

    public List<UserPreferenceMemory> getByUserId(Long userId) {
        return userPreferenceMemoryMapper.selectByUserId(userId);
    }

    public List<UserPreferenceMemory> getAll() {
        return userPreferenceMemoryMapper.selectAll();
    }

    public int create(UserPreferenceMemory memory) {
        return userPreferenceMemoryMapper.insert(memory);
    }

    public int delete(Long id) {
        return userPreferenceMemoryMapper.deleteById(id);
    }

    public int deleteByUserId(Long userId) {
        return userPreferenceMemoryMapper.deleteByUserId(userId);
    }
}
