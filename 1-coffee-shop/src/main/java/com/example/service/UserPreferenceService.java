package com.example.service;

import com.example.entity.UserPreference;
import com.example.mapper.UserPreferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserPreferenceService {

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    public UserPreference getByUserId(Long userId) {
        return userPreferenceMapper.selectByUserId(userId);
    }

    public List<UserPreference> getAll() {
        return userPreferenceMapper.selectAll();
    }

    public int create(UserPreference preference) {
        return userPreferenceMapper.insert(preference);
    }

    public int update(UserPreference preference) {
        return userPreferenceMapper.update(preference);
    }

    public int delete(Long userId) {
        return userPreferenceMapper.deleteByUserId(userId);
    }
}
