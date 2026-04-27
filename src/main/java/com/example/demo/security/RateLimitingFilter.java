package com.example.demo.security;

import com.example.demo.config.RateLimiterConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter using Google Guava RateLimiter
 * Applies rate limiting to incoming requests based on client IP and endpoint
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterConfig rateLimiterConfig;

    private static final String[] EXCLUDED_PATHS = {
        "/api/health/",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/actuator/"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip rate limiting for excluded paths
        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get client IP (handle proxies)
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        try {
            // Check if request is allowed
            if (rateLimiterConfig.allowRequest(clientIp, endpoint)) {
                // Request allowed
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                long waitTime = rateLimiterConfig.getWaitTimeSeconds(endpoint);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.addHeader("Retry-After", String.valueOf(waitTime));
                
                String errorResponse = String.format(
                    "{\"error\": \"Rate limit exceeded\", \"retryAfter\": %d, \"message\": \"Please wait %d seconds before making another request\"}",
                    waitTime,
                    waitTime
                );
                response.getWriter().write(errorResponse);
                
                log.warn("[RATE-LIMIT] Request denied for IP: {} Endpoint: {} Wait: {}s", clientIp, endpoint, waitTime);
            }
        } catch (Exception e) {
            log.error("[RATE-LIMIT] Error processing rate limit: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Extract client IP address from request (handles proxies)
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",      // Proxy header
            "CF-Connecting-IP",     // Cloudflare
            "True-Client-IP",       // Cloudflare
            "X-Real-IP"             // Nginx reverse proxy
        };

        for (String headerName : headerNames) {
            String value = request.getHeader(headerName);
            if (value != null && !value.isBlank()) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                return value.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Check if path should be excluded from rate limiting
     */
    private boolean isExcludedPath(String requestPath) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't filter requests to excluded paths
        String requestPath = request.getRequestURI();
        return isExcludedPath(requestPath);
    }
}
