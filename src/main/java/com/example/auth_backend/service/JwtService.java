package com.example.auth_backend.service;

import com.example.auth_backend.entity.User;
import java.util.Map;

/**
 * Service responsible for generating and validating JWT access tokens.
 * Supports HMAC (HS256) when a secret is provided, or RSA (RS256) when
 * PEM-encoded keys are supplied via properties.
 */
public interface JwtService {
    /**
     * Generate an access token for the given user. The token payload will include
     * sub (user id), username (email), roles, iat and exp.
     */
    String generateAccessToken(User user);

    /**
     * Validate a token's signature and expiration.
     */
    boolean validateAccessToken(String token);

    /**
     * Parse claims from a token. Returns a map of claim name to value.
     */
    Map<String, Object> parseClaims(String token);
}
