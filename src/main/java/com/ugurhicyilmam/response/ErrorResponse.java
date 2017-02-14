package com.ugurhicyilmam.response;

import lombok.Value;

@Value
public class ErrorResponse {

    private final String status;
    private final String message;
    private final String code;

    public ErrorResponse(String code, String message) {
        this.status = "ERROR";
        this.code = code;
        this.message = message;
    }
}
