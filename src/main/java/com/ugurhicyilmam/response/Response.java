package com.ugurhicyilmam.response;

import lombok.Data;

@Data
public class Response<T> {
    private ResponseCode code;
    private T data;

    public Response(ResponseCode code) {
        this.code = code;
        this.data = null;
    }

    public Response(ResponseCode code, T data) {
        this.code = code;
        this.data = data;
    }

    public Response() {
        this.code = ResponseCode.SUCCESS;
        this.data = null;
    }
}
