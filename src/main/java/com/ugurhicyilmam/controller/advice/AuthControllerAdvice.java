package com.ugurhicyilmam.controller.advice;

import com.ugurhicyilmam.response.Response;
import com.ugurhicyilmam.response.Status;
import com.ugurhicyilmam.service.exceptions.InvalidActivationTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class AuthControllerAdvice {

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response processValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return Response.builder(Status.FAIL).data(errors).build();
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(InvalidActivationTokenException.class)
    @ResponseBody
    public Response processActivationTokenError(InvalidActivationTokenException ex) {
        return Response.builder(Status.FAIL).message("Activation token is not valid").build();
    }
}
