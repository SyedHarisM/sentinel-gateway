package com.example.sentinel_gateway.fallback;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.example.sentinel_gateway.exception.ErrorResponse;

@RestController
public class FallbackController {

    private static final Logger logger =
            LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/fallback/products")
    public ResponseEntity<ErrorResponse> productFallback(ServerWebExchange exchange) {

        logger.warn("Product Service is unavailable. Returning fallback response.");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "Product Service is temporarily unavailable. Please try again later.",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getHeaders().getFirst("X-Correlation-ID")
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}