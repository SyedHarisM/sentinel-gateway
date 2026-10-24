package com.example.sentinel_gateway.exception;

public class ForbiddenException extends GatewayException {
    public ForbiddenException(String message){
        super(message);
    }
}
