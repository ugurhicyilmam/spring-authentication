package com.ugurhicyilmam.service;


import com.ugurhicyilmam.model.User;

public interface UserService {
    User findByEmail(String email);

    void create(User user);

    void activateUser(User user);
}
