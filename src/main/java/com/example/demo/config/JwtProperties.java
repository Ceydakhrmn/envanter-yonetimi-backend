package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * Maps jwt.* properties from application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    /**
     * Secret key for JWT signing (minimum 256 bits)
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds
     */
    private long expiration;

    /**
     * Refresh token expiration time in milliseconds
     */
    private long refreshExpiration;
}
