package com.ugurhicyilmam.service;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.controller.request.ResetRequest;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.transfer.LoginTransfer;

public interface AuthService {

    User register(RegisterRequest request);

    void activate(String token);

    void resendActivationToken(User user);

    LoginTransfer login(LoginRequest request);

    void recover(String email);

    LoginTransfer reset(String recoveryToken, String password);

    LoginTransfer refresh(String refreshToken);

    void logout(String refreshToken);

}
