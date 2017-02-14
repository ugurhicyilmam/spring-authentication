package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User findByEmail(String email) {
        return null;
    }
}
