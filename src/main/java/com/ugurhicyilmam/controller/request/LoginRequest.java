package com.ugurhicyilmam.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString(exclude = {"password"})
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {
        //empty body
    }

}
