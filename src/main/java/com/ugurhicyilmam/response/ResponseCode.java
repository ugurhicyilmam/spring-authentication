package com.ugurhicyilmam.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseCode {

    SUCCESS("S00"),
    ERR_VALIDATION("E01"),
    ERR_INVALID_ACTIVATION_TOKEN("E02"),
    ERR_INVALID_ACCESS_TOKEN("E03"),
    ERR_LOGIN_FAILED("E04"),
    ERR_INVALID_RECOVERY_TOKEN("E05"),
    ERR_INVALID_REFRESH_TOKEN("E06"),
    ERR_USER_NOT_FOUND("E07");


    private String value;

    ResponseCode(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
