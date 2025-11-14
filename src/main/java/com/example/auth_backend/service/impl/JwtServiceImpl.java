package com.example.auth_backend.service.impl;

import com.example.auth_backend.entity.User;
import com.example.auth_backend.service.JwtService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final String hmacSecret;
    private final long expirationSeconds;

    private final MACSigner macSigner;
    private final MACVerifier macVerifier;
    private final JWSAlgorithm alg = JWSAlgorithm.HS256;

    public JwtServiceImpl(
            @Value("${jwt.secret:}") String hmacSecret,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.hmacSecret = hmacSecret != null ? hmacSecret.trim() : "";
        this.expirationSeconds = expirationSeconds > 0 ? expirationSeconds : 3600L;

        if (this.hmacSecret.isEmpty()) {
            throw new IllegalStateException("Missing required property 'jwt.secret' — provide a long random secret in application properties or environment");
        }

        byte[] secretBytes = this.hmacSecret.getBytes(StandardCharsets.UTF_8);
        try {
            this.macSigner = new MACSigner(secretBytes);
            this.macVerifier = new MACVerifier(secretBytes);
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to initialize HMAC signer/verifier", e);
        }
    }

    @Override
    public String generateAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(expirationSeconds);

            String role = user.getRole() != null ? user.getRole().name() : "USER";
            List<String> roles = Collections.singletonList(role);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUserId() != null ? user.getUserId().toString() : null)
                    .claim("username", user.getEmail())
                    .claim("roles", roles)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(alg), claims);
            signedJWT.sign(macSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign JWT", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            SignedJWT signed = SignedJWT.parse(token);

            if (!signed.verify(macVerifier)) return false;
            Date exp = signed.getJWTClaimsSet().getExpirationTime();
            return exp != null && exp.after(new Date());
        } catch (ParseException | JOSEException e) {
            log.debug("Token validation/parsing failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> parseClaims(String token) {
        try {
            SignedJWT signed = SignedJWT.parse(token);
            JWTClaimsSet claims = signed.getJWTClaimsSet();
            Map<String, Object> result = new HashMap<>();
            for (String name : claims.getClaims().keySet()) {
                result.put(name, claims.getClaim(name));
            }
            // standard claims
            if (claims.getSubject() != null) result.put("sub", claims.getSubject());
            if (claims.getIssueTime() != null) result.put("iat", claims.getIssueTime().getTime() / 1000);
            if (claims.getExpirationTime() != null) result.put("exp", claims.getExpirationTime().getTime() / 1000);
            return result;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}
