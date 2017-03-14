package com.ugurhicyilmam.controller.advice;

import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.service.exceptions.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ugurhicyilmam.response.ResponseCode.*;

@ControllerAdvice
@ResponseBody
public class AuthControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response processValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return new Response<>(ERR_VALIDATION, errors);
    }

    @ExceptionHandler(InvalidActivationTokenException.class)
    public Response handleError(InvalidActivationTokenException ex) {
        return new Response(ERR_INVALID_ACTIVATION_TOKEN);
    }

    @ExceptionHandler(InvalidAccessTokenException.class)
    public Response handleError(InvalidAccessTokenException ex) {
        return new Response(ERR_INVALID_ACCESS_TOKEN);
    }

    @ExceptionHandler(InvalidRecoveryTokenException.class)
    public Response handleError(InvalidRecoveryTokenException ex) {
        return new Response(ERR_INVALID_RECOVERY_TOKEN);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public Response handleError(InvalidRefreshTokenException ex) {
        return new Response(ERR_INVALID_REFRESH_TOKEN);
    }

    @ExceptionHandler(LoginFailedException.class)
    public Response handleError(LoginFailedException ex) {
        return new Response(ERR_LOGIN_FAILED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Response handleError(UserNotFoundException ex) {
        return new Response(ERR_USER_NOT_FOUND);
    }
}
