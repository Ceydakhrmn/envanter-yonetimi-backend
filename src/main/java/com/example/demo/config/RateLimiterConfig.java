package com.example.demo.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiter Configuration using Google Guava RateLimiter
 * Manages API rate limits by endpoint using token bucket algorithm
 */
@Component
public class RateLimiterConfig {

    private final Cache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(10000)
        .build();

    /**
     * Check if request is allowed and get rate limiter for the given key
     * @param key Unique identifier (usually IP address)
     * @param endpoint API endpoint path
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String key, String endpoint) {
        String limitKey = endpoint + ":" + key;
        double permitsPerSecond = getPermitsPerSecond(endpoint);
        
        RateLimiter rateLimiter = limiters.asMap().computeIfAbsent(limitKey, k -> 
            RateLimiter.create(permitsPerSecond)
        );
        
        return rateLimiter.tryAcquire();
    }

    /**
     * Get permits per second based on endpoint
     */
    private double getPermitsPerSecond(String endpoint) {
        if (endpoint.contains("/api/auth/login")) {
            // 5 requests per minute = 0.083 per second
            return 0.083;
        } else if (endpoint.contains("/api/auth/register")) {
            // 10 requests per hour = 0.0028 per second
            return 0.0028;
        } else if (endpoint.contains("/api/")) {
            // General API: 100 requests per minute = 1.67 per second
            return 1.67;
        } else {
            // Default: 500 requests per minute = 8.33 per second
            return 8.33;
        }
    }

    /**
     * Get wait time in seconds for the given endpoint
     */
    public long getWaitTimeSeconds(String endpoint) {
        double permitsPerSecond = getPermitsPerSecond(endpoint);
        // Return ~60 seconds for all endpoints as a standard wait time
        return 60;
    }
}
