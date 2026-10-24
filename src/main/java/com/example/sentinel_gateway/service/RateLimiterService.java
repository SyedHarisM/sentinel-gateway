package com.example.sentinel_gateway.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.sentinel_gateway.exception.RateLimitExceededException;
import com.example.sentinel_gateway.properties.RateLimitProperties;

import reactor.core.publisher.Mono;

@Service
public class RateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    public RateLimiterService(ReactiveStringRedisTemplate redisTemplate,
                              RateLimitProperties rateLimitProperties) {

        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;
    }

    public Mono<Void> isAllowed(String username, String path) {

        if (!rateLimitProperties.isEnabled()) {
            return Mono.empty();
        }

        String key = buildRateLimitKey(username);

        return redisTemplate.opsForValue()

                .increment(key)

                .flatMap(counter -> {

                    Mono<Boolean> ttlOperation = Mono.just(Boolean.TRUE);

                    if (counter == 1) {
                        ttlOperation = redisTemplate.expire(
                                key,
                                Duration.ofSeconds(rateLimitProperties.getWindowSeconds())
                        );
                    }

                    return ttlOperation.then(validateRequest(counter));
                });

    }

    private Mono<Void> validateRequest(Long counter) {

        if (counter > rateLimitProperties.getMaxRequests()) {
            return Mono.error(
                    new RateLimitExceededException("Rate limit exceeded.")
            );
        }

        return Mono.empty();
    }

    private String buildRateLimitKey(String username) {
        return RATE_LIMIT_PREFIX + username;
    }

}