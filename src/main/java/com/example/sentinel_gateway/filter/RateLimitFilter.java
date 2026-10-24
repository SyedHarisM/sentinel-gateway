package com.example.sentinel_gateway.filter;

import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.sentinel_gateway.service.RateLimiterService;

import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        System.out.println("RateLimitFilter Executed");

        ServerHttpRequest request = exchange.getRequest();

        String path = request.getPath().value();

        if (PUBLIC_ENDPOINTS.contains(path)) {
            return chain.filter(exchange);
        }

        String username = request.getHeaders().getFirst("X-Auth-User");

        if (username == null) {
            return chain.filter(exchange);
        }

        return rateLimiterService
                .isAllowed(username, path)
                .then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}