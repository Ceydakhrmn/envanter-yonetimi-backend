package com.example.demo.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter for authentication endpoints.
 * Limits requests per IP address to prevent brute-force attacks.
 *
 * - /api/auth/login: 10 requests per minute per IP
 * - /api/auth/forgot-password: 3 requests per minute per IP
 * - /api/auth/register: 5 requests per minute per IP
 * - /api/invitations/accept: 5 requests per minute per IP
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        Bucket bucket = null;

        if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = loginBuckets.computeIfAbsent(clientIp, k -> createBucket(10, 1));
        } else if (path.equals("/api/auth/forgot-password") && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = forgotPasswordBuckets.computeIfAbsent(clientIp, k -> createBucket(3, 1));
        } else if ((path.equals("/api/auth/register") || path.startsWith("/api/invitations/accept"))
                && "POST".equalsIgnoreCase(request.getMethod())) {
            bucket = registerBuckets.computeIfAbsent(clientIp, k -> createBucket(5, 1));
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Creates a token bucket with the given capacity and refill rate.
     * @param capacity max tokens
     * @param refillMinutes refill interval in minutes
     */
    private Bucket createBucket(int capacity, int refillMinutes) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
                        .build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
