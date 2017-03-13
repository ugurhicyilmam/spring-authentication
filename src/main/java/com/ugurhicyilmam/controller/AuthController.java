package com.ugurhicyilmam.controller;

import com.ugurhicyilmam.controller.request.ChangePasswordRequest;
import com.ugurhicyilmam.controller.request.LoginRequest;
import com.ugurhicyilmam.controller.request.RegisterRequest;
import com.ugurhicyilmam.controller.request.ResetRequest;
import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.service.AuthService;
import com.ugurhicyilmam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

import static com.ugurhicyilmam.response.ResponseCode.SUCCESS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @RequestMapping(method = POST, value = "/register")
    public Response register(@RequestBody @Valid RegisterRequest request) {
        User user = authService.register(request);
        return new Response(SUCCESS, authService.login(user));
    }

    @RequestMapping(method = GET, value = "/activate")
    public Response activate(@RequestParam String token) {
        authService.activate(token);
        return new Response(SUCCESS);
    }

    @RequestMapping(method = POST, value = "/login")
    public Response login(@RequestBody LoginRequest request) {
        return new Response(SUCCESS, authService.login(request));
    }

    @RequestMapping(method = GET, value = "/recover")
    public Response recover(@RequestParam String email) {
        authService.recover(email);
        return new Response(SUCCESS);
    }

    @RequestMapping(method = POST, value = "/reset")
    public Response reset(@RequestParam String recoveryToken, @RequestBody @Valid ResetRequest request) {
        User user = authService.reset(recoveryToken, request.getPassword());
        return new Response(SUCCESS, authService.login(user));
    }

    @RequestMapping(method = GET, value = "/refresh")
    public Response refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        return new Response(SUCCESS, authService.refresh(refreshToken));
    }

    @RequestMapping(method = GET, value = "/logout")
    public Response logout(@RequestHeader("Refresh-Token") String refreshToken, @RequestHeader("Access-Token") String accessToken) {
        authService.logout(refreshToken, accessToken);
        return new Response(SUCCESS);
    }

    @RequestMapping(method = GET, value = "/change-password")
    public Response changePassword(@RequestBody @Valid ChangePasswordRequest request, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        authService.changePassword(user, request.getCurrentPassword(), request.getPassword());
        return new Response(SUCCESS);
    }
}
