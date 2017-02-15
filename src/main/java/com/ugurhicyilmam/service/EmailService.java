package com.ugurhicyilmam.service;

import com.ugurhicyilmam.model.User;

public interface EmailService {
    void sendActivationEmail(User user);
}
