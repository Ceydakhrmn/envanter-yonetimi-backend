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
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable Long id, @RequestBody AssetRequestDTO dto) {
        AssetResponseDTO updated = assetService.update(id, dto);
        activityLogService.log("UPDATE", "ASSET", id, "Varlık güncellendi: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        activityLogService.log("DELETE", "ASSET", id, "Varlık silindi");
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
