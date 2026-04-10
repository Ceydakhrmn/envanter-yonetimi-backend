package com.example.demo.controller;

import com.example.demo.dto.AssetRequestDTO;
import com.example.demo.dto.AssetResponseDTO;
import com.example.demo.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final com.example.demo.service.ActivityLogService activityLogService;
    private final com.example.demo.service.NotificationService notificationService;
    private final com.example.demo.repository.KullaniciRepository kullaniciRepository;

    @GetMapping
    public ResponseEntity<List<AssetResponseDTO>> getAll() {
        return ResponseEntity.ok(assetService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping
    public ResponseEntity<AssetResponseDTO> create(@RequestBody AssetRequestDTO dto) {
        AssetResponseDTO created = assetService.create(dto);
        activityLogService.log("CREATE", "ASSET", created.getId(), "Varlık oluşturuldu: " + created.getName());
        // Notify admins about new asset
        List<String> adminEmails = kullaniciRepository.findByActiveTrue().stream()
                .filter(u -> u.getRole() == com.example.demo.entity.Kullanici.Role.ADMIN)
                .map(com.example.demo.entity.Kullanici::getEmail).toList();
        notificationService.notifyAllAdmins("info", "Yeni varlık oluşturuldu: " + created.getName(), adminEmails);
        // Notify assigned user
        if (dto.getAssignedUserId() != null) {
            kullaniciRepository.findById(dto.getAssignedUserId()).ifPresent(u ->
                notificationService.create("info", "Size yeni bir varlık atandı: " + created.getName(), u.getEmail()));
        }
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable Long id, @RequestBody AssetRequestDTO dto) {
        AssetResponseDTO updated = assetService.update(id, dto);
        activityLogService.log("UPDATE", "ASSET", id, "Varlık güncellendi: " + updated.getName());
        // Notify assigned user about update
        if (dto.getAssignedUserId() != null) {
            kullaniciRepository.findById(dto.getAssignedUserId()).ifPresent(u ->
                notificationService.create("info", "Atanmış varlık güncellendi: " + updated.getName(), u.getEmail()));
        }
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AssetResponseDTO asset = assetService.getById(id);
        assetService.delete(id);
        activityLogService.log("DELETE", "ASSET", id, "Varlık silindi");
        // Notify admins about asset deletion
        List<String> adminEmails = kullaniciRepository.findByActiveTrue().stream()
                .filter(u -> u.getRole() == com.example.demo.entity.Kullanici.Role.ADMIN)
                .map(com.example.demo.entity.Kullanici::getEmail).toList();
        notificationService.notifyAllAdmins("warning", "Varlık silindi: " + asset.getName(), adminEmails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<AssetResponseDTO>> getExpiringSoon() {
        return ResponseEntity.ok(assetService.getExpiringSoon());
    }

    @GetMapping("/search")
    public ResponseEntity<List<AssetResponseDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(assetService.search(q));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(assetService.getStats());
    }
}
