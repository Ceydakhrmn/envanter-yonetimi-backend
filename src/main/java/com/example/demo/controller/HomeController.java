package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Ana sayfa controller - API bilgilendirme
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Corporate Inventory and User Management System");
        response.put("version", "1.0.0");
        response.put("status", "Running ✅");
        response.put("api_base_url", "/api/kullanicilar");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/kullanicilar", "List all users");
        endpoints.put("GET /api/kullanicilar/aktif", "List active users");
        endpoints.put("GET /api/kullanicilar/{id}", "Get user by ID");
        endpoints.put("GET /api/kullanicilar/email/{email}", "Get user by email");
        endpoints.put("GET /api/kullanicilar/departman/{dept}", "List by department");
        endpoints.put("POST /api/kullanicilar", "Create new user");
        endpoints.put("PUT /api/kullanicilar/{id}", "Update user");
        endpoints.put("DELETE /api/kullanicilar/{id}", "Delete user (soft)");
        endpoints.put("GET /api/kullanicilar/health", "Health check");
        
        response.put("endpoints", endpoints);
        
        Map<String, Object> sampleUser = new HashMap<>();
        sampleUser.put("ad", "Ahmet");
        sampleUser.put("soyad", "Yılmaz");
        sampleUser.put("email", "ahmet@efsora.com");
        sampleUser.put("departman", "IT");
        
        response.put("sample_post_body", sampleUser);
        
        return response;
    }
}
