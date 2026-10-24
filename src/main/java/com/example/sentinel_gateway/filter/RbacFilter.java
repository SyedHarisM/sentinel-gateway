package com.example.sentinel_gateway.filter;

import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.sentinel_gateway.exception.ForbiddenException;
import com.example.sentinel_gateway.security.Role;
import com.example.sentinel_gateway.service.AuthorizationService;

import reactor.core.publisher.Mono;

@Component
public class RbacFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    private final AuthorizationService authorizationService;

    public RbacFilter(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        if (PUBLIC_ENDPOINTS.contains(path)) {
            return chain.filter(exchange);
        }

        String roleHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Auth-Role");

        if (roleHeader == null) {
            throw new ForbiddenException("User role not found.");
        }

        Role role = Role.valueOf(roleHeader);

        if (!authorizationService.isAuthorized(role, path)) {
            throw new ForbiddenException("Access denied.");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}