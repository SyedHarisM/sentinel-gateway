package com.example.sentinel_gateway.exception;

public class InvalidJwtException extends GatewayException {

    public InvalidJwtException(String message){
        super(message);
    } 
}
