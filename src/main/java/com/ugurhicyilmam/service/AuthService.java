package com.ugurhicyilmam.service;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.model.User;

public interface AuthService {
    User register(RegisterRequest request);
}
