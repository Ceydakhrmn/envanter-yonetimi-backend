package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
@Slf4j
public class JwtProperties {

    private String secret;
    private long expiration;
    private long refreshExpiration;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isBlank() || secret.contains("default-dev-secret")) {
            log.warn("⚠️  JWT secret is using default value! Set JWT_SECRET environment variable in production.");
        }
        if (secret != null && secret.length() < 32) {
            log.warn("⚠️  JWT secret is too short ({}). Use at least 32 characters.", secret.length());
        }
    }
}
