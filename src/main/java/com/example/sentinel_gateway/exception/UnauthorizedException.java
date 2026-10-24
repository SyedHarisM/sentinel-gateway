package com.example.sentinel_gateway.exception;

public class UnauthorizedException extends GatewayException{
    public UnauthorizedException(String message){
        super(message);
    }
    
}
