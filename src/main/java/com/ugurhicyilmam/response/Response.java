package com.ugurhicyilmam.response;

import lombok.Value;

@Value
public class Response {
    private Status status;
    private Object data;

    public Response(Status status, Object data) {
        this.status = status;
        this.data = data;
    }
}
