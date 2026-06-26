package com.example.demo.controller;

import com.example.demo.entity.ActivityLog;
import com.example.demo.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getRecent() {
        return ResponseEntity.ok(activityLogService.getRecent());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ActivityLog>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = (startDate != null && !startDate.isBlank())
                ? LocalDateTime.parse(startDate + "T00:00:00") : null;
        LocalDateTime end = (endDate != null && !endDate.isBlank())
                ? LocalDateTime.parse(endDate + "T23:59:59") : null;
        return ResponseEntity.ok(activityLogService.getAll(page, size, start, end));
    }

    @GetMapping("/user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLog>> getByUser(@PathVariable String email) {
        return ResponseEntity.ok(activityLogService.getByUser(email));
    }

    @GetMapping("/entity/{type}")
    public ResponseEntity<List<ActivityLog>> getByEntityType(@PathVariable String type) {
        return ResponseEntity.ok(activityLogService.getByEntityType(type));
    }

    @GetMapping("/security-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getSecurityStats() {
        return ResponseEntity.ok(activityLogService.getSecurityStats());
    }
}
