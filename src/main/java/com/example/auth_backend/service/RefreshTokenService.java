package com.example.auth_backend.service;

import com.example.auth_backend.entity.RefreshToken;
import com.example.auth_backend.entity.User;

public interface RefreshTokenService {
    /**
     * Create a new refresh token for the given user. Returns the raw token string (not hashed).
     */
    String createToken(User user);

    /**
     * Validate a raw refresh token and return the matching RefreshToken entity if valid.
     * Throws a ResponseStatusException(HttpStatus.UNAUTHORIZED) when invalid.
     */
    RefreshToken validateRefreshToken(String rawToken);

    /**
     * Rotate (replace) an existing refresh token: revoke the old one and create a new one
     * for the same user. Returns the new raw token string.
     */
    String rotateRefreshToken(String oldRawToken);

    /** Revoke a single refresh token by its raw value. */
    void revokeRefreshToken(String rawToken);

    /** Revoke (or remove) all refresh tokens belonging to the given user. */
    void revokeAllForUser(User user);
}
