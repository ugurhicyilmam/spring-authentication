package com.ugurhicyilmam.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Response {
    private Status status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResponseCode code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    private Response(Status status, Object data, ResponseCode code, String message) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static ResponseBuilder builder(Status status) {
        return new ResponseBuilder(status);
    }

    public static class ResponseBuilder {

        private Status status;
        private Object data;
        private ResponseCode code;
        private String message;

        private ResponseBuilder(Status status) {
            this.status = status;
        }

        public ResponseBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public ResponseBuilder code(ResponseCode code) {
            this.code = code;
            return this;
        }

        public ResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public Response build() {
            return new Response(status, data, code, message);
        }
    }

}
