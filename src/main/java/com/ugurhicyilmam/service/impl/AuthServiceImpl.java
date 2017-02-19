package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.event.OnAccountCreation;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.ActivationTokenService;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.UserService;
import com.ugurhicyilmam.util.enums.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final ActivationTokenService activationTokenService;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public AuthServiceImpl(UserService userService, ActivationTokenService activationTokenService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.activationTokenService = activationTokenService;
    }

    @Override
    public User register(RegisterRequest request) {
        User user = initializeUserByRegisterRequest(request);
        userService.create(user);
        activationTokenService.issueNewToken(user);
        eventPublisher.publishEvent(new OnAccountCreation(user));
        return user;
    }

    private User initializeUserByRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());
        user.setLanguage(Language.valueOf(request.getLanguage()));
        return user;
    }
}
