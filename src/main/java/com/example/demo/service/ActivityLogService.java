package com.example.demo.service;

import com.example.demo.entity.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
}
