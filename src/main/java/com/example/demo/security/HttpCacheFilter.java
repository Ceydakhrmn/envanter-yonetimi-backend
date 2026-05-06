package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP Cache Control Filter
 * Adds appropriate cache headers to API responses for performance optimization
 */
@Component
public class HttpCacheFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    
    String requestPath = request.getRequestURI();
    String method = request.getMethod();

    // Static assets - Long cache (1 year)
    if (requestPath.startsWith("/swagger-ui") || requestPath.startsWith("/v3/api-docs")) {
      response.setHeader("Cache-Control", "public, max-age=31536000");
    }
    // Health check - No cache
    else if (requestPath.startsWith("/actuator/health") || requestPath.startsWith("/api/health")) {
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    }
    // GET requests - Cache for 5 minutes
    else if ("GET".equalsIgnoreCase(method)) {
      response.setHeader("Cache-Control", "public, max-age=300");
    }
    // POST, PUT, DELETE - No cache
    else {
      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
      response.setHeader("Pragma", "no-cache");
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // Apply filter to all requests
    return false;
  }
}
