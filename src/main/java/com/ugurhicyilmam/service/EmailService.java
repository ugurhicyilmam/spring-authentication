package com.ugurhicyilmam.service;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.impl.EmailServiceImpl;

import java.util.Map;

public interface EmailService {
    void sendActivationEmail(User user);

    void sendEmailTo(User user, EmailServiceImpl.EmailType emailType, Map<String, Object> emailArguments);

    void sendWelcomeEmail(User user);
}
