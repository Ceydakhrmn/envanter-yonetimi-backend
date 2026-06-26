package com.example.demo.controller;

import com.example.demo.dto.AssetRequestDTO;
import com.example.demo.dto.AssetResponseDTO;
import com.example.demo.dto.DepreciationResponse;
import com.example.demo.dto.DepreciationSummaryResponse;
import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.entity.AssetAssignmentHistory;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetAssignmentHistoryRepository;
import com.example.demo.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Asset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final com.example.demo.service.ActivityLogService activityLogService;
    private final com.example.demo.service.NotificationService notificationService;
    private final com.example.demo.repository.KullaniciRepository kullaniciRepository;
    private final AssetAssignmentHistoryRepository assignmentHistoryRepository;
    private final com.example.demo.service.DepreciationService depreciationService;

    @GetMapping
    public ResponseEntity<PagedResponseDTO<AssetResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AssetResponseDTO> allAssets = assetService.getAll();
        
        // Simple in-memory pagination
        int totalElements = allAssets.size();
        int totalPages = (totalElements + size - 1) / size;
        
        // Validate page
        if (page < 0) page = 0;
        if (page >= totalPages && totalElements > 0) page = totalPages - 1;
        
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<AssetResponseDTO> pageContent = allAssets.subList(start, end);
        
        PagedResponseDTO<AssetResponseDTO> response = PagedResponseDTO.<AssetResponseDTO>builder()
            .content(pageContent)
            .currentPage(page)
            .pageSize(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(page < totalPages - 1)
            .hasPrevious(page > 0)
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping
    public ResponseEntity<AssetResponseDTO> create(@RequestBody AssetRequestDTO dto, Authentication auth) {
        AssetResponseDTO created = assetService.create(dto);
        activityLogService.log("CREATE", "ASSET", created.getId(), "Varlık oluşturuldu: " + created.getName());

        List<String> adminEmails = kullaniciRepository.findByActiveTrue().stream()
                .filter(u -> u.getRole() == Kullanici.Role.ADMIN)
                .map(Kullanici::getEmail).toList();
        notificationService.notifyAllAdmins("info", "Yeni varlık oluşturuldu: " + created.getName(), adminEmails);

        // Track initial assignment
        if (dto.getAssignedUserId() != null) {
            kullaniciRepository.findById(dto.getAssignedUserId()).ifPresent(u -> {
                notificationService.create("info", "Size yeni bir varlık atandı: " + created.getName(), u.getEmail());
                logAssignment(created.getId(), created.getName(), "ASSIGNED",
                        null, null, u.getId(), u.getFirstName() + " " + u.getLastName(),
                        null, dto.getAssignedDepartment(), auth.getName());
            });
        }
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable Long id, @RequestBody AssetRequestDTO dto, Authentication auth) {
        // Get current state before update
        AssetResponseDTO current = assetService.getById(id);
        Long oldUserId = current.getAssignedUserId();
        String oldUserName = current.getAssignedUserName();
        String oldDept = current.getAssignedDepartment();

        AssetResponseDTO updated = assetService.update(id, dto);
        activityLogService.log("UPDATE", "ASSET", id, "Varlık güncellendi: " + updated.getName());

        Long newUserId = dto.getAssignedUserId();
        String newDept = dto.getAssignedDepartment();

        // Detect assignment change
        boolean userChanged = !Objects.equals(oldUserId, newUserId);
        boolean deptChanged = !Objects.equals(oldDept, newDept);

        if (userChanged || deptChanged) {
            String newUserName = null;
            if (newUserId != null) {
                Kullanici newUser = kullaniciRepository.findById(newUserId).orElse(null);
                if (newUser != null) newUserName = newUser.getFirstName() + " " + newUser.getLastName();
            }

            String action;
            if (oldUserId == null && newUserId != null) {
                action = "ASSIGNED";
            } else if (oldUserId != null && newUserId == null) {
                action = "UNASSIGNED";
            } else {
                action = "REASSIGNED";
            }

            logAssignment(id, updated.getName(), action,
                    oldUserId, oldUserName, newUserId, newUserName,
                    oldDept, newDept, auth.getName());

            // Notify old user (unassigned)
            if (oldUserId != null && userChanged) {
                kullaniciRepository.findById(oldUserId).ifPresent(u ->
                    notificationService.create("warning", "Varlık atamanız kaldırıldı: " + updated.getName(), u.getEmail()));
            }
            // Notify new user (assigned)
            if (newUserId != null && userChanged) {
                kullaniciRepository.findById(newUserId).ifPresent(u ->
                    notificationService.create("info", "Size bir varlık atandı: " + updated.getName(), u.getEmail()));
            }
        } else if (newUserId != null) {
            // Same user, just update notification
            kullaniciRepository.findById(newUserId).ifPresent(u ->
                notificationService.create("info", "Atanmış varlık güncellendi: " + updated.getName(), u.getEmail()));
        }

        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> transfer(@PathVariable Long id, @RequestBody Map<String, Long> body, Authentication auth) {
        Long newUserId = body.get("userId");
        if (newUserId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId required"));

        AssetResponseDTO current = assetService.getById(id);
        Long oldUserId = current.getAssignedUserId();
        String oldUserName = current.getAssignedUserName();

        Kullanici newUser = kullaniciRepository.findById(newUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        AssetRequestDTO dto = new AssetRequestDTO();
        dto.setName(current.getName()); dto.setCategory(current.getCategory());
        dto.setStatus(current.getStatus()); dto.setBrand(current.getBrand());
        dto.setModel(current.getModel()); dto.setVendor(current.getVendor());
        dto.setSerialNumber(current.getSerialNumber()); dto.setPurchaseDate(current.getPurchaseDate());
        dto.setPurchasePrice(current.getPurchasePrice()); dto.setWarrantyExpiryDate(current.getWarrantyExpiryDate());
        dto.setRenewalDate(current.getRenewalDate()); dto.setSeatCount(current.getSeatCount());
        dto.setAssignedUserId(newUserId); dto.setAssignedDepartment(current.getAssignedDepartment());
        dto.setNotes(current.getNotes());

        AssetResponseDTO updated = assetService.update(id, dto);
        String newUserName = newUser.getFirstName() + " " + newUser.getLastName();

        activityLogService.log("REASSIGNED", "ASSET", id,
            "Varlık devredildi: " + updated.getName() + " → " + newUserName);
        logAssignment(id, updated.getName(), oldUserId == null ? "ASSIGNED" : "REASSIGNED",
            oldUserId, oldUserName, newUserId, newUserName,
            current.getAssignedDepartment(), current.getAssignedDepartment(), auth.getName());

        if (oldUserId != null && !oldUserId.equals(newUserId)) {
            kullaniciRepository.findById(oldUserId).ifPresent(u ->
                notificationService.create("warning", "Varlık atamanız kaldırıldı: " + updated.getName(), u.getEmail()));
        }
        notificationService.create("info", "Size bir varlık devredildi: " + updated.getName(), newUser.getEmail());

        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnAsset(@PathVariable Long id, Authentication auth) {
        AssetResponseDTO current = assetService.getById(id);
        Long oldUserId = current.getAssignedUserId();
        String oldUserName = current.getAssignedUserName();

        AssetRequestDTO dto = new AssetRequestDTO();
        dto.setName(current.getName()); dto.setCategory(current.getCategory());
        dto.setStatus(current.getStatus()); dto.setBrand(current.getBrand());
        dto.setModel(current.getModel()); dto.setVendor(current.getVendor());
        dto.setSerialNumber(current.getSerialNumber()); dto.setPurchaseDate(current.getPurchaseDate());
        dto.setPurchasePrice(current.getPurchasePrice()); dto.setWarrantyExpiryDate(current.getWarrantyExpiryDate());
        dto.setRenewalDate(current.getRenewalDate()); dto.setSeatCount(current.getSeatCount());
        dto.setAssignedUserId(null); dto.setAssignedDepartment(current.getAssignedDepartment());
        dto.setNotes(current.getNotes());

        AssetResponseDTO updated = assetService.update(id, dto);
        activityLogService.log("UNASSIGNED", "ASSET", id, "Varlık iade edildi: " + updated.getName());
        logAssignment(id, updated.getName(), "UNASSIGNED",
            oldUserId, oldUserName, null, null,
            current.getAssignedDepartment(), current.getAssignedDepartment(), auth.getName());

        if (oldUserId != null) {
            kullaniciRepository.findById(oldUserId).ifPresent(u ->
                notificationService.create("warning", "Varlık atamanız kaldırıldı: " + updated.getName(), u.getEmail()));
        }
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AssetResponseDTO asset = assetService.getById(id);
        assetService.delete(id);
        activityLogService.log("DELETE", "ASSET", id, "Varlık silindi");

        List<String> adminEmails = kullaniciRepository.findByActiveTrue().stream()
                .filter(u -> u.getRole() == Kullanici.Role.ADMIN)
                .map(Kullanici::getEmail).toList();
        notificationService.notifyAllAdmins("warning", "Varlık silindi: " + asset.getName(), adminEmails);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PostMapping("/bulk-import")
    public ResponseEntity<Map<String, Object>> bulkImport(@RequestBody List<AssetRequestDTO> assets) {
        if (assets == null || assets.isEmpty()) return ResponseEntity.badRequest().build();
        List<Map<String, Object>> results = assetService.bulkImport(assets);
        long success = results.stream().filter(r -> "success".equals(r.get("status"))).count();
        long failed = results.stream().filter(r -> "error".equals(r.get("status"))).count();
        activityLogService.log("BULK_IMPORT", "ASSET", null, "Toplu içe aktarma: " + success + " başarılı, " + failed + " başarısız");
        return ResponseEntity.ok(Map.of("total", assets.size(), "success", success, "failed", failed, "results", results));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ResponseEntity.badRequest().build();
        assetService.bulkDelete(ids);
        activityLogService.log("BULK_DELETE", "ASSET", null, ids.size() + " varlık silindi");
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @PatchMapping("/bulk-status")
    public ResponseEntity<Void> bulkUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        String statusStr = (String) body.get("status");
        if (rawIds == null || rawIds.isEmpty() || statusStr == null) return ResponseEntity.badRequest().build();
        List<Long> ids = rawIds.stream().map(Integer::longValue).toList();
        Asset.Status status = Asset.Status.valueOf(statusStr);
        assetService.bulkUpdateStatus(ids, status);
        activityLogService.log("UPDATE", "ASSET", null, ids.size() + " varlığın durumu güncellendi: " + statusStr);
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

    @GetMapping("/{id}/assignment-history")
    public ResponseEntity<List<AssetAssignmentHistory>> getAssignmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentHistoryRepository.findByAssetIdOrderByCreatedAtDesc(id));
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(assetService.getAllTags());
    }

    @GetMapping("/by-tag")
    public ResponseEntity<List<AssetResponseDTO>> getByTag(@RequestParam String tag) {
        return ResponseEntity.ok(assetService.getByTag(tag));
    }

    @GetMapping("/depreciation-summary")
    public ResponseEntity<DepreciationSummaryResponse> getDepreciationSummary() {
        return ResponseEntity.ok(depreciationService.getSummary());
    }

    @GetMapping("/{id}/depreciation")
    public ResponseEntity<DepreciationResponse> getDepreciation(@PathVariable Long id) {
        Asset asset = assetService.getEntityById(id);
        if (asset == null) return ResponseEntity.notFound().build();
        DepreciationResponse resp = depreciationService.calculate(asset);
        if (resp == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping(value = "/{id}/qr-code", produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long id) {
        AssetResponseDTO asset = assetService.getById(id);
        try {
            String data = "EFSORA-ASSET|ID:" + asset.getId() + "|" + asset.getName()
                    + (asset.getSerialNumber() != null ? "|SN:" + asset.getSerialNumber() : "");
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=asset-" + id + "-qr.png")
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void logAssignment(Long assetId, String assetName, String action,
                               Long fromUserId, String fromUserName,
                               Long toUserId, String toUserName,
                               String fromDept, String toDept, String performedBy) {
        AssetAssignmentHistory h = new AssetAssignmentHistory();
        h.setAssetId(assetId);
        h.setAssetName(assetName);
        h.setAction(action);
        h.setFromUserId(fromUserId);
        h.setFromUserName(fromUserName);
        h.setToUserId(toUserId);
        h.setToUserName(toUserName);
        h.setFromDepartment(fromDept);
        h.setToDepartment(toDept);
        h.setPerformedBy(performedBy);
        assignmentHistoryRepository.save(h);
    }
}
