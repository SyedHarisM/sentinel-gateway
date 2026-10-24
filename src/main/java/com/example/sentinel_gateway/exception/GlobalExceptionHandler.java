package com.example.sentinel_gateway.exception;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof InvalidJwtException
                || ex instanceof MissingTokenException
                || ex instanceof JwtExpiredException) {

            status = HttpStatus.UNAUTHORIZED;

        } else if (ex instanceof ForbiddenException) {

            status = HttpStatus.FORBIDDEN;

        } else if (ex instanceof RateLimitExceededException) {

            status = HttpStatus.TOO_MANY_REQUESTS;
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();

        String correlationId =
                (String) exchange.getAttributes().get(CORRELATION_ID_ATTRIBUTE);

        String message = ex.getMessage() != null
                ? ex.getMessage()
                : status.getReasonPhrase();

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                method,
                path,
                correlationId
        );

        try {

            String json = objectMapper.writeValueAsString(errorResponse);

            DataBuffer buffer = response.bufferFactory()
                    .wrap(json.getBytes(StandardCharsets.UTF_8));

            if (status.is5xxServerError()) {

                logger.error(
                        "Request Failed | CorrelationId={} | Method={} | Path={} | Status={} | Message={}",
                        correlationId,
                        method,
                        path,
                        status.value(),
                        message,
                        ex
                );

            } else {

                logger.warn(
                        "Request Failed | CorrelationId={} | Method={} | Path={} | Status={} | Message={}",
                        correlationId,
                        method,
                        path,
                        status.value(),
                        message
                );
            }

            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {

            logger.error("Failed to serialize error response", e);

            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String fallbackJson = """
                    {
                      "status":500,
                      "error":"Internal Server Error",
                      "message":"Unable to serialize error response"
                    }
                    """;

            DataBuffer buffer = response.bufferFactory()
                    .wrap(fallbackJson.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        }
    }
}