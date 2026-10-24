package com.example.sentinel_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {


    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        String correlationId = UUID.randomUUID().toString();


        exchange.getAttributes()
                .put(
                    CORRELATION_ID_ATTRIBUTE,
                    correlationId
                );


        ServerHttpRequest mutatedRequest =
                exchange.getRequest()
                        .mutate()
                        .header(
                            CORRELATION_ID_HEADER,
                            correlationId
                        )
                        .build();


        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(mutatedRequest)
                        .build();


        mutatedExchange.getResponse()
                .getHeaders()
                .add(
                    CORRELATION_ID_HEADER,
                    correlationId
                );


        return chain.filter(mutatedExchange);
    }


    @Override
    public int getOrder() {
        return -3;
    }
}