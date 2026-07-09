package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.Kullanici;
import com.example.demo.entity.LicenseSeatAssignment;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.repository.LicenseSeatAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class LicenseSeatAssignmentController {

    private final LicenseSeatAssignmentRepository seatRepository;
    private final AssetRepository assetRepository;
    private final KullaniciRepository kullaniciRepository;

    @GetMapping("/{assetId}/seat-assignments")
    public ResponseEntity<List<LicenseSeatAssignment>> list(@PathVariable Long assetId) {
        return ResponseEntity.ok(seatRepository.findByAssetIdOrderByAssignedAtAsc(assetId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{assetId}/seat-assignments")
    public ResponseEntity<?> assign(
            @PathVariable Long assetId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) return ResponseEntity.notFound().build();

        if (asset.getSeatCount() == null || asset.getSeatCount() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bu varlığın koltuk sayısı tanımlı değil."));
        }

        Long userId = body.get("userId") instanceof Number n ? n.longValue() : null;
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId gerekli."));

        if (seatRepository.existsByAssetIdAndUserId(assetId, userId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kullanıcı zaten bu lisansa atanmış."));
        }

        long used = seatRepository.countByAssetId(assetId);
        if (used >= asset.getSeatCount()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tüm koltukllar dolu. (" + used + "/" + asset.getSeatCount() + ")"));
        }

        Kullanici user = kullaniciRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        LicenseSeatAssignment s = new LicenseSeatAssignment();
        s.setAssetId(assetId);
        s.setAssetName(asset.getName());
        s.setUserId(userId);
        s.setUserName(user.getFirstName() + " " + user.getLastName());
        s.setUserEmail(user.getEmail());
        s.setAssignedBy(auth.getName());

        return ResponseEntity.ok(seatRepository.save(s));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @DeleteMapping("/seat-assignments/{id}")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        if (!seatRepository.existsById(id)) return ResponseEntity.notFound().build();
        seatRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
