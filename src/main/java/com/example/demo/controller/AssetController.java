package com.example.demo.controller;

import com.example.demo.dto.AssetRequestDTO;
import com.example.demo.dto.AssetResponseDTO;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final com.example.demo.service.ActivityLogService activityLogService;
    private final com.example.demo.service.NotificationService notificationService;
    private final com.example.demo.repository.KullaniciRepository kullaniciRepository;
    private final AssetAssignmentHistoryRepository assignmentHistoryRepository;

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
