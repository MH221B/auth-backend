package com.example.auth_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for JWT settings. This enables IDE/property metadata
 * generation so custom properties like jwt.expiration-seconds are recognized.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    /**
     * Shared HMAC secret for HS256. Use environment variables for production.
     */
    private String secret;

    /**
     * Expiration time in seconds for access tokens.
     */
    private long expirationSeconds = 3600;

    private Rsa rsa = new Rsa();

    @Data
    public static class Rsa {
        /** PEM encoded private key (PKCS#8) */
        private String privateKey;
        /** PEM encoded public key (X.509) */
        private String publicKey;
    }
}
