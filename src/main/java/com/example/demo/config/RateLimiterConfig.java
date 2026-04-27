package com.example.demo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter Configuration using Bucket4j
 * Manages API rate limits by endpoint
 */
@Configuration
public class RateLimiterConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Get or create a bucket for the given key
     * @param key Unique identifier (usually IP address or user ID)
     * @param endpoint API endpoint path
     * @return Bucket for rate limiting
     */
    public Bucket resolveBucket(String key, String endpoint) {
        String bucketKey = endpoint + ":" + key;
        return buckets.computeIfAbsent(bucketKey, k -> createBucket(endpoint));
    }

    /**
     * Create a bucket based on the endpoint
     * Different endpoints have different rate limits
     */
    private Bucket createBucket(String endpoint) {
        if (endpoint.contains("/api/auth/login")) {
            // 5 requests per minute for login (brute force protection)
            return Bucket4j.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
        } else if (endpoint.contains("/api/auth/register")) {
            // 10 requests per hour for registration
            return Bucket4j.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1))))
                .build();
        } else if (endpoint.contains("/api/")) {
            // General API: 100 requests per minute
            return Bucket4j.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
        } else {
            // Default: 500 requests per minute
            return Bucket4j.builder()
                .addLimit(Bandwidth.classic(500, Refill.intervally(500, Duration.ofMinutes(1))))
                .build();
        }
    }

    /**
     * Get remaining tokens for display in response header
     */
    public long getRemainingTokens(String key, String endpoint) {
        Bucket bucket = resolveBucket(key, endpoint);
        return bucket.estimateAbilityToConsume(1).getRoundedTokensToConsume();
    }
}
