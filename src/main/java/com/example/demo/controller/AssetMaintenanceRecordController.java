package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.AssetMaintenanceRecord;
import com.example.demo.repository.AssetMaintenanceRecordRepository;
import com.example.demo.repository.AssetRepository;
import com.example.demo.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetMaintenanceRecordController {

    private final AssetMaintenanceRecordRepository maintenanceRecordRepository;
    private final AssetRepository assetRepository;
    private final ActivityLogService activityLogService;

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{assetId}/maintenance-records")
    public ResponseEntity<?> create(
            @PathVariable Long assetId,
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }

        AssetMaintenanceRecord record = new AssetMaintenanceRecord();
        record.setAssetId(assetId);
        record.setAssetName(asset.getName());
        record.setDescription((String) body.get("description"));
        record.setMaintenanceDate(LocalDate.parse((String) body.get("maintenanceDate")));
        record.setPerformedBy((String) body.get("performedBy"));
        Object costValue = body.get("cost");
        if (costValue != null && !costValue.toString().isBlank()) {
            record.setCost(new BigDecimal(costValue.toString()));
        }
        record.setLoggedBy(authentication.getName());

        AssetMaintenanceRecord saved = maintenanceRecordRepository.save(record);

        activityLogService.log("CREATE", "MAINTENANCE_RECORD", assetId,
                "Bakım kaydı eklendi: " + asset.getName());

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{assetId}/maintenance-records")
    public ResponseEntity<List<AssetMaintenanceRecord>> list(@PathVariable Long assetId) {
        return ResponseEntity.ok(maintenanceRecordRepository.findByAssetIdOrderByMaintenanceDateDesc(assetId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @DeleteMapping("/maintenance-records/{recordId}")
    public ResponseEntity<Void> delete(@PathVariable Long recordId) {
        if (!maintenanceRecordRepository.existsById(recordId)) {
            return ResponseEntity.notFound().build();
        }
        maintenanceRecordRepository.deleteById(recordId);
        return ResponseEntity.noContent().build();
    }
}
