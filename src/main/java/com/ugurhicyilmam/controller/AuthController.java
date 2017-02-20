package com.ugurhicyilmam.controller;

import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @RequestMapping(method = POST, value = "/register")
    public Response register(@RequestBody @Valid RegisterRequest request) {
        User user = authService.register(request);
        return null;
    }

    @RequestMapping(method = GET, value = "/activate")
    public Response activate(@RequestParam String token) {
        authService.activate(token);
        System.out.println(token);
        return null;
    }

}
