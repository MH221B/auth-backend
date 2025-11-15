package com.example.auth_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for application cookies.
 * This enables IDE/property metadata generation so `app.cookie.secure` is recognized.
 */
@Component
@ConfigurationProperties(prefix = "app.cookie")
@Data
public class CookieProperties {
    /**
     * Enable Secure flag for cookies (sent only over HTTPS).
     */
    private boolean secure = false;
}
