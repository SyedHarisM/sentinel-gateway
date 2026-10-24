package com.example.sentinel_gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT.
 *
 * Values are loaded from:
 *
 * jwt:
 *   secret: your-secret-key
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key used to sign and verify JWT tokens.
     */
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}