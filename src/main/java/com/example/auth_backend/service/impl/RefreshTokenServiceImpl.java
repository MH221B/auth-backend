package com.example.auth_backend.service.impl;

import com.example.auth_backend.entity.RefreshToken;
import com.example.auth_backend.entity.User;
import com.example.auth_backend.repository.RefreshTokenRepository;
import com.example.auth_backend.service.RefreshTokenService;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    // Token generation settings
    private static final int RAW_TOKEN_BYTE_LENGTH = 64; // 512 bits
    private static final long EXPIRATION_DAYS = 30L;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String createToken(User user) {
        String raw = generateRawToken();
        String hash = hashToken(raw);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshTokenId(UUID.randomUUID());
        refreshToken.setTokenHash(hash);
        refreshToken.setExpiryDate(Instant.now().plus(EXPIRATION_DAYS, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);

        try {
            refreshTokenRepository.save(refreshToken);
            return raw;
        } catch (DataIntegrityViolationException e) {
            return createToken(user);
        }
    }

    @Override
    public RefreshToken validateRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(hash);
        RefreshToken token = maybe.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked or expired");
        }

        return token;
    }

    @Override
    @Transactional
    public String rotateRefreshToken(String oldRawToken) {
        RefreshToken existing = validateRefreshToken(oldRawToken);
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return createToken(existing.getUser());
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(hash);
        maybe.ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(User user) {
        // repository provides deleteByUser — use it to remove tokens tied to the user
        refreshTokenRepository.deleteByUser(user);
    }

    // --- helpers ---
    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available; wrap in runtime exception if not
            throw new IllegalStateException("SHA-256 MessageDigest not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
