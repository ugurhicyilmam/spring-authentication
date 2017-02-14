package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public AuthServiceImpl(UserService userService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User register(RegisterRequest request) {
        User user = initializeUserByRegisterRequest(request);
        userService.create(user);
        eventPublisher.publishEvent(new OnAccountCreation(user));
        return user;
    }

    private User initializeUserByRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());
        return user;
    }
}
