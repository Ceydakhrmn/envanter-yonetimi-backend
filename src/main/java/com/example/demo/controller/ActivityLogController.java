package com.example.demo.controller;

import com.example.demo.entity.ActivityLog;
import com.example.demo.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityLogService.getAll(page, size));
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
}
