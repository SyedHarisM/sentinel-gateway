package com.example.sentinel_gateway.exception;

public class JwtExpiredException extends GatewayException {
    public JwtExpiredException(String message){
        super(message);
    }
    
}
