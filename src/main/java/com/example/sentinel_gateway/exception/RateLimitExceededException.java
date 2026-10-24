package com.example.sentinel_gateway.exception;

public class RateLimitExceededException extends GatewayException{
    public RateLimitExceededException(String message){
        super(message);
    }
    
}
