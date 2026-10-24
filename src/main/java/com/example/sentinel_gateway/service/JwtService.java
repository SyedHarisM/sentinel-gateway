package com.example.sentinel_gateway.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.sentinel_gateway.exception.InvalidJwtException;
import com.example.sentinel_gateway.exception.JwtExpiredException;
import com.example.sentinel_gateway.properties.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Validates the JWT token and returns its claims.
     *
     * @param token JWT token
     * @return JWT claims
     * @throws JwtExpiredException if token has expired
     * @throws InvalidJwtException if token is invalid
     */
    public Claims extractClaims(String token) {

        try {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {

            throw new JwtExpiredException("JWT token has expired");

        } catch (JwtException e) {

            throw new InvalidJwtException("Invalid JWT token");
        }
    }
}