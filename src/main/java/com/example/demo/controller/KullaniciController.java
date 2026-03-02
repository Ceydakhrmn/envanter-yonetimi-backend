package com.example.demo.controller;

import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcı CRUD işlemleri için API endpoint'leri")
public class KullaniciController {

    private final KullaniciService kullaniciService;

    @Operation(summary = "Tüm kullanıcıları listele", description = "Sistemdeki tüm kullanıcıları (aktif ve pasif) listeler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Başarılı",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Kullanici.class)))
    })
    @GetMapping
    public ResponseEntity<List<Kullanici>> tumKullanicilar() {
        log.info("API: Tüm kullanıcılar listeleniyor");
        return ResponseEntity.ok(kullaniciService.tumKullanicilar());
    }

    @Operation(summary = "Aktif kullanıcıları listele", description = "Sadece aktif (silinmemiş) kullanıcıları listeler")
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/aktif")
    public ResponseEntity<List<Kullanici>> aktifKullanicilar() {
        log.info("API: Aktif kullanıcılar listeleniyor");
        return ResponseEntity.ok(kullaniciService.aktifKullanicilar());
    }

    @Operation(summary = "ID ile kullanıcı bul", description = "Belirli bir kullanıcının detaylarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı bulundu"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciBul(
            @Parameter(description = "Kullanıcı ID", example = "1") @PathVariable Long id) {
        log.info("API: Kullanıcı aranıyor ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciBul(id));
    }

    @Operation(summary = "Email ile kullanıcı bul", description = "Email adresine göre kullanıcı arar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı bulundu"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Kullanici> emailIleKullaniciBul(
            @Parameter(description = "Email adresi", example = "ahmet@efsora.com") @PathVariable String email) {
        log.info("API: Email ile kullanıcı aranıyor: {}", email);
        return ResponseEntity.ok(kullaniciService.emailIleKullaniciBul(email));
    }

    @Operation(summary = "Departmana göre listele", description = "Belirli bir departmandaki kullanıcıları listeler")
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/departman/{departman}")
    public ResponseEntity<List<Kullanici>> departmanaGoreListele(
            @Parameter(description = "Departman adı", example = "IT") @PathVariable String departman) {
        log.info("API: Departmana göre listeleniyor: {}", departman);
        return ResponseEntity.ok(kullaniciService.departmanaGoreListele(departman));
    }

    @Operation(summary = "Yeni kullanıcı oluştur", description = "Sisteme yeni kullanıcı ekler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Kullanıcı başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz veri veya email zaten kullanımda")
    })
    @PostMapping
    public ResponseEntity<Kullanici> kullaniciOlustur(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yeni kullanıcı bilgileri",
                required = true
            ) @Valid @RequestBody Kullanici kullanici) {
        log.info("API: Yeni kullanıcı oluşturuluyor: {}", kullanici.getEmail());
        Kullanici yeniKullanici = kullaniciService.kullaniciOlustur(kullanici);
        return ResponseEntity.status(HttpStatus.CREATED).body(yeniKullanici);
    }

    @Operation(summary = "Kullanıcı güncelle", description = "Mevcut kullanıcının bilgilerini günceller")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı güncellendi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "400", description = "Geçersiz veri")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciGuncelle(
            @Parameter(description = "Kullanıcı ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Güncellenmiş kullanıcı bilgileri",
                required = true
            ) @Valid @RequestBody Kullanici kullanici) {
        log.info("API: Kullanıcı güncelleniyor ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciGuncelle(id, kullanici));
    }

    @Operation(summary = "Kullanıcıyı sil (soft delete)", description = "Kullanıcıyı pasif hale getirir, veriler korunur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Kullanıcı silindi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kullaniciSil(
            @Parameter(description = "Kullanıcı ID", example = "1") @PathVariable Long id) {
        log.info("API: Kullanıcı siliniyor (deaktif) ID={}", id);
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Kullanıcıyı kalıcı sil", description = "Kullanıcıyı veritabanından tamamen siler (geri alınamaz!)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Kullanıcı kalıcı olarak silindi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    @DeleteMapping("/{id}/kalici")
    public ResponseEntity<Void> kullaniciKaliciSil(
            @Parameter(description = "Kullanıcı ID", example = "1") @PathVariable Long id) {
        log.warn("API: Kullanıcı kalıcı olarak siliniyor ID={}", id);
        kullaniciService.kullaniciKaliciSil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check", description = "API'nin çalışır durumda olup olmadığını kontrol eder")
    @ApiResponse(responseCode = "200", description = "API çalışıyor")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Kullanıcı API çalışıyor! ✅");
    }
}
