package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetMaintenanceSchedule;
import com.example.demo.repository.AssetMaintenanceScheduleRepository;
import com.example.demo.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetMaintenanceScheduleController {

    private final AssetMaintenanceScheduleRepository scheduleRepository;
    private final AssetRepository assetRepository;

    @GetMapping("/{assetId}/maintenance-schedule")
    public ResponseEntity<List<AssetMaintenanceSchedule>> list(@PathVariable Long assetId) {
        return ResponseEntity.ok(scheduleRepository.findByAssetIdOrderByScheduledDateAsc(assetId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{assetId}/maintenance-schedule")
    public ResponseEntity<?> create(
            @PathVariable Long assetId,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) return ResponseEntity.notFound().build();

        String scheduledDateStr = body.get("scheduledDate");
        if (scheduledDateStr == null || scheduledDateStr.isBlank()) return ResponseEntity.badRequest().build();

        AssetMaintenanceSchedule s = new AssetMaintenanceSchedule();
        s.setAssetId(assetId);
        s.setAssetName(asset.getName());
        s.setScheduledDate(LocalDate.parse(scheduledDateStr));
        s.setDescription(body.get("description"));
        s.setStatus("PENDING");
        s.setCreatedBy(auth.getName());

        return ResponseEntity.ok(scheduleRepository.save(s));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/maintenance-schedule/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {
        return scheduleRepository.findById(id).map(s -> {
            s.setStatus("COMPLETED");
            s.setCompletedAt(LocalDate.now());
            return ResponseEntity.ok(scheduleRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @DeleteMapping("/maintenance-schedule/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) return ResponseEntity.notFound().build();
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
