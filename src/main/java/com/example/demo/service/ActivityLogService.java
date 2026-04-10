package com.example.demo.service;

import com.example.demo.entity.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public void log(String action, String entityType, Long entityId, String details) {
        try {
            ActivityLog entry = new ActivityLog();
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setDetails(details);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                entry.setUserEmail(auth.getName());
                entry.setUserName(auth.getName());
            } else {
                entry.setUserEmail("system");
                entry.setUserName("System");
            }

            // IP ve User-Agent bilgisi
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    String ip = request.getHeader("X-Forwarded-For");
                    if (ip == null || ip.isBlank()) {
                        ip = request.getRemoteAddr();
                    } else {
                        ip = ip.split(",")[0].trim();
                    }
                    entry.setIpAddress(ip);
                    entry.setUserAgent(request.getHeader("User-Agent"));
                }
            } catch (Exception ignored) {}

            repository.save(entry);
            log.debug("Activity logged: {} {} {} by {}", action, entityType, entityId, entry.getUserEmail());
        } catch (Exception e) {
            log.warn("Failed to log activity: {}", e.getMessage());
        }
    }

    public List<ActivityLog> getRecent() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Page<ActivityLog> getAll(int page, int size) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public List<ActivityLog> getByUser(String email) {
        return repository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    public List<ActivityLog> getByEntityType(String entityType) {
        return repository.findByEntityTypeOrderByCreatedAtDesc(entityType);
    }

    public java.util.Map<String, Object> getSecurityStats() {
        java.time.LocalDateTime last24h = java.time.LocalDateTime.now().minusHours(24);
        java.time.LocalDateTime last7d = java.time.LocalDateTime.now().minusDays(7);

        long failedLogins24h = repository.countByActionAndCreatedAtAfter("LOGIN_FAILED", last24h);
        long accountLocks24h = repository.countByActionAndCreatedAtAfter("ACCOUNT_LOCKED", last24h);
        long successLogins24h = repository.countByActionAndCreatedAtAfter("LOGIN", last24h);
        long blockedLogins24h = repository.countByActionAndCreatedAtAfter("LOGIN_BLOCKED", last24h);
        List<String> suspiciousIps = repository.findSuspiciousIpsSince(last24h);
        List<ActivityLog> recentSecurityEvents = repository.findByActionInAndCreatedAtAfterOrderByCreatedAtDesc(
            java.util.List.of("LOGIN_FAILED", "ACCOUNT_LOCKED", "LOGIN_BLOCKED"), last7d
        );

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("failedLogins24h", failedLogins24h);
        stats.put("accountLocks24h", accountLocks24h);
        stats.put("successLogins24h", successLogins24h);
        stats.put("blockedLogins24h", blockedLogins24h);
        stats.put("suspiciousIps", suspiciousIps);
        stats.put("recentSecurityEvents", recentSecurityEvents);
        return stats;
    }
}
