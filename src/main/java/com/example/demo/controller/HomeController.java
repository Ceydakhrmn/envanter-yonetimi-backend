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
        response.put("uygulama", "Kurumsal Envanter ve Kullanıcı Yönetim Sistemi");
        response.put("versiyon", "1.0.0");
        response.put("durum", "Çalışıyor ✅");
        response.put("api_base_url", "/api/kullanicilar");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/kullanicilar", "Tüm kullanıcılar");
        endpoints.put("GET /api/kullanicilar/aktif", "Aktif kullanıcılar");
        endpoints.put("GET /api/kullanicilar/{id}", "ID ile kullanıcı");
        endpoints.put("GET /api/kullanicilar/email/{email}", "Email ile kullanıcı");
        endpoints.put("GET /api/kullanicilar/departman/{dept}", "Departmana göre");
        endpoints.put("POST /api/kullanicilar", "Yeni kullanıcı oluştur");
        endpoints.put("PUT /api/kullanicilar/{id}", "Kullanıcı güncelle");
        endpoints.put("DELETE /api/kullanicilar/{id}", "Kullanıcı sil (soft)");
        endpoints.put("GET /api/kullanicilar/health", "Health check");
        
        response.put("endpoints", endpoints);
        
        Map<String, Object> ornekKullanici = new HashMap<>();
        ornekKullanici.put("ad", "Ahmet");
        ornekKullanici.put("soyad", "Yılmaz");
        ornekKullanici.put("email", "ahmet@efsora.com");
        ornekKullanici.put("departman", "IT");
        
        response.put("ornek_post_body", ornekKullanici);
        
        return response;
    }
}
