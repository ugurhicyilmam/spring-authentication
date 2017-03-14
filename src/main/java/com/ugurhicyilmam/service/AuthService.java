package com.ugurhicyilmam.service;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.transfer.LoginTransfer;

public interface AuthService {

    User register(RegisterRequest request);

    void activate(String token);

    void resendActivationToken(User user);

    LoginTransfer login(LoginRequest request);

    void recover(String email);

    User reset(String recoveryToken, String password);

    LoginTransfer refresh(String refreshToken);

    void changePassword(User user, String currentPassword, String password);

    LoginTransfer login(User user);

    void logout(String refreshToken, String accessToken);

    User getUserByValidAccessToken(String accessToken);

}
