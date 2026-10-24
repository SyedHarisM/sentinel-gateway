package com.example.sentinel_gateway.exception;

public class MissingTokenException extends GatewayException{
    public MissingTokenException(String message){
        super(message);
    }
    
}
