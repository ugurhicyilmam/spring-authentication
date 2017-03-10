package com.ugurhicyilmam.controller;

import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.controller.request.ResetRequest;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.response.Status;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.transfer.LoginTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        LoginTransfer loginTransfer = authService.login(new LoginRequest(request.getEmail(), request.getPassword()));
        return Response.builder(Status.SUCCESS).data(loginTransfer).build();
    }

    @RequestMapping(method = GET, value = "/activate")
    public Response activate(@RequestParam String token) {
        authService.activate(token);
        return Response.builder(Status.SUCCESS).build();
    }

    @RequestMapping(method = POST, value = "/login")
    public Response login(@RequestBody LoginRequest request) {
        return Response.builder(Status.SUCCESS).data(authService.login(request)).build();
    }

    @RequestMapping(method = GET, value = "/recover")
    public Response recover(@RequestParam String email) {
        authService.recover(email);
        return Response.builder(Status.SUCCESS).build();
    }

    @RequestMapping(method = POST, value = "/reset")
    public Response reset(@RequestParam String recoveryToken, @RequestBody @Valid ResetRequest request) {
        return Response.builder(Status.SUCCESS).data(authService.reset(recoveryToken, request.getPassword())).build();
    }

    @RequestMapping(method = GET, value = "/refresh")
    public Response refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        return Response.builder(Status.SUCCESS).data(authService.refresh(refreshToken)).build();
    }

    @RequestMapping(method = GET, value = "/logout")
    public Response logout(@RequestHeader("Refresh-Token") String refreshToken) {
        authService.logout(refreshToken);
        return Response.builder(Status.SUCCESS).build();
    }

}
