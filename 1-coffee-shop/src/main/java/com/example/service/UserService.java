package com.example.service;

import com.example.entity.User;

import java.util.List;

public interface UserService {
    User getById(Long id);

    User getByUsername(String username);

    List<User> getAll();

    int create(User user);

    int update(User user);

//    int delete(Long id);
}
