package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Slf4j
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Operation(summary = "Health check", description = "Simple health check endpoint to warm up the server")
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        log.info("Health check ping received");
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis() + "");
        return ResponseEntity.ok(response);
    }
}
