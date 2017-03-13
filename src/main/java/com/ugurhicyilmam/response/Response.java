package com.ugurhicyilmam.response;

import lombok.Data;

@Data
public class Response {
    private ResponseCode code;
    private Object data = null;

    public Response(ResponseCode code) {
        this.code = code;
    }

    public Response(ResponseCode code, Object data) {
        this.code = code;
        this.data = data;
    }
}
