package com.example.sentinel_gateway.filter;

import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.sentinel_gateway.exception.InvalidJwtException;
import com.example.sentinel_gateway.exception.MissingTokenException;
import com.example.sentinel_gateway.service.JwtService;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String path = request.getPath().value();

        // Skip authentication for public endpoints
        if (PUBLIC_ENDPOINTS.contains(path)) {
            return chain.filter(exchange);
        }

        // Authorization header validation
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            throw new MissingTokenException("Authorization header is missing");
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidJwtException("Invalid Authorization header format");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // Validate JWT and extract claims
        Claims claims = jwtService.extractClaims(token);

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-User", username)
                .header("X-Auth-Role", role)
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(mutatedRequest)
                        .build()
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}