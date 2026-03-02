package com.example.demo.controller;

import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kullanıcı Controller - REST API endpoint'leri
 * Frontend buradan veri alır ve gönderir
 */
@RestController
@RequestMapping("/api/kullanicilar")
@RequiredArgsConstructor
@Slf4j
public class KullaniciController {

    private final KullaniciService kullaniciService;

    /**
     * Tüm kullanıcıları listele
     * GET /api/kullanicilar
     */
    @GetMapping
    public ResponseEntity<List<Kullanici>> tumKullanicilar() {
        log.info("API: Tüm kullanıcılar listeleniyor");
        return ResponseEntity.ok(kullaniciService.tumKullanicilar());
    }

    /**
     * Aktif kullanıcıları listele
     * GET /api/kullanicilar/aktif
     */
    @GetMapping("/aktif")
    public ResponseEntity<List<Kullanici>> aktifKullanicilar() {
        log.info("API: Aktif kullanıcılar listeleniyor");
        return ResponseEntity.ok(kullaniciService.aktifKullanicilar());
    }

    /**
     * ID ile kullanıcı bul
     * GET /api/kullanicilar/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciBul(@PathVariable Long id) {
        log.info("API: Kullanıcı aranıyor ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciBul(id));
    }

    /**
     * Email ile kullanıcı bul
     * GET /api/kullanicilar/email/ornek@efsora.com
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Kullanici> emailIleKullaniciBul(@PathVariable String email) {
        log.info("API: Email ile kullanıcı aranıyor: {}", email);
        return ResponseEntity.ok(kullaniciService.emailIleKullaniciBul(email));
    }

    /**
     * Departmana göre kullanıcıları listele
     * GET /api/kullanicilar/departman/IT
     */
    @GetMapping("/departman/{departman}")
    public ResponseEntity<List<Kullanici>> departmanaGoreListele(@PathVariable String departman) {
        log.info("API: Departmana göre listeleniyor: {}", departman);
        return ResponseEntity.ok(kullaniciService.departmanaGoreListele(departman));
    }

    /**
     * Yeni kullanıcı oluştur
     * POST /api/kullanicilar
     * Body: {"ad":"Ahmet","soyad":"Yılmaz","email":"ahmet@efsora.com","departman":"IT"}
     */
    @PostMapping
    public ResponseEntity<Kullanici> kullaniciOlustur(@Valid @RequestBody Kullanici kullanici) {
        log.info("API: Yeni kullanıcı oluşturuluyor: {}", kullanici.getEmail());
        Kullanici yeniKullanici = kullaniciService.kullaniciOlustur(kullanici);
        return ResponseEntity.status(HttpStatus.CREATED).body(yeniKullanici);
    }

    /**
     * Kullanıcı güncelle
     * PUT /api/kullanicilar/1
     * Body: {"ad":"Ahmet","soyad":"Yılmaz","email":"ahmet@efsora.com","departman":"HR"}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody Kullanici kullanici) {
        log.info("API: Kullanıcı güncelleniyor ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciGuncelle(id, kullanici));
    }

    /**
     * Kullanıcıyı sil (soft delete)
     * DELETE /api/kullanicilar/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kullaniciSil(@PathVariable Long id) {
        log.info("API: Kullanıcı siliniyor (deaktif) ID={}", id);
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Kullanıcıyı kalıcı sil (hard delete)
     * DELETE /api/kullanicilar/1/kalici
     */
    @DeleteMapping("/{id}/kalici")
    public ResponseEntity<Void> kullaniciKaliciSil(@PathVariable Long id) {
        log.warn("API: Kullanıcı kalıcı olarak siliniyor ID={}", id);
        kullaniciService.kullaniciKaliciSil(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sağlık kontrolü
     * GET /api/kullanicilar/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Kullanıcı API çalışıyor! ✅");
    }
}
