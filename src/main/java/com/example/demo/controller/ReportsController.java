package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.KullaniciRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Reporting endpoints for dashboard analytics")
public class ReportsController {

    private final AssetRepository assetRepository;
    private final KullaniciRepository kullaniciRepository;

    @Operation(summary = "Department summary", description = "Returns per-department user and asset statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @GetMapping("/department-summary")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentSummary() {
        log.info("API: Department summary requested");

        List<Kullanici> allUsers = kullaniciRepository.findAll();
        List<Asset> allAssets = assetRepository.findAll();

        Map<String, List<Kullanici>> usersByDept = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getDepartment() != null ? u.getDepartment() : "Unknown"));

        Map<String, List<Asset>> assetsByDept = allAssets.stream()
                .filter(a -> a.getAssignedDepartment() != null)
                .collect(Collectors.groupingBy(Asset::getAssignedDepartment));

        List<Map<String, Object>> result = new ArrayList<>();
        for (String dept : usersByDept.keySet()) {
            Map<String, Object> row = new HashMap<>();
            List<Kullanici> deptUsers = usersByDept.getOrDefault(dept, List.of());
            List<Asset> deptAssets = assetsByDept.getOrDefault(dept, List.of());

            long activeAssets = deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.ACTIVE).count();
            long maintenanceAssets = deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.MAINTENANCE).count();
            long expiredAssets = deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.EXPIRED).count();
            double totalValue = deptAssets.stream()
                    .filter(a -> a.getPurchasePrice() != null)
                    .mapToDouble(a -> a.getPurchasePrice().doubleValue())
                    .sum();
            Map<String, Long> byCategory = deptAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getCategory().name(), Collectors.counting()));

            row.put("department", dept);
            row.put("totalUsers", deptUsers.size());
            row.put("activeUsers", deptUsers.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count());
            row.put("totalAssets", deptAssets.size());
            row.put("activeAssets", activeAssets);
            row.put("maintenanceAssets", maintenanceAssets);
            row.put("expiredAssets", expiredAssets);
            row.put("totalValue", totalValue);
            row.put("byCategory", byCategory);
            result.add(row);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Asset overview", description = "Returns overall asset statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @GetMapping("/asset-overview")
    public ResponseEntity<Map<String, Object>> getAssetOverview() {
        log.info("API: Asset overview requested");

        List<Asset> all = assetRepository.findAll();
        Map<String, Object> result = new HashMap<>();

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(a -> a.getCategory().name(), Collectors.counting()));

        long assigned = all.stream().filter(a -> a.getAssignedUserId() != null).count();
        long expiringSoon = all.stream()
                .filter(a -> a.getRenewalDate() != null
                        && a.getStatus() == Asset.Status.ACTIVE
                        && a.getRenewalDate().isBefore(LocalDate.now().plusDays(30)))
                .count();
        double totalValue = all.stream()
                .filter(a -> a.getPurchasePrice() != null)
                .mapToDouble(a -> a.getPurchasePrice().doubleValue())
                .sum();

        Map<String, Long> monthlyAcquisition = all.stream()
                .filter(a -> a.getPurchaseDate() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getPurchaseDate().getYear() + "-" + String.format("%02d", a.getPurchaseDate().getMonthValue()),
                        Collectors.counting()));

        result.put("total", all.size());
        result.put("byStatus", byStatus);
        result.put("byCategory", byCategory);
        result.put("assigned", assigned);
        result.put("unassigned", all.size() - assigned);
        result.put("expiringSoon", expiringSoon);
        result.put("totalValue", totalValue);
        result.put("monthlyAcquisition", monthlyAcquisition);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "User overview", description = "Returns overall user statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @GetMapping("/user-overview")
    public ResponseEntity<Map<String, Object>> getUserOverview() {
        log.info("API: User overview requested");

        List<Kullanici> all = kullaniciRepository.findAll();
        Map<String, Object> result = new HashMap<>();

        long active = all.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count();
        Map<String, Long> byDepartment = all.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(Kullanici::getDepartment, Collectors.counting()));
        Map<String, Long> byRole = all.stream()
                .filter(u -> u.getRole() != null)
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));

        result.put("total", all.size());
        result.put("active", active);
        result.put("inactive", all.size() - active);
        result.put("byDepartment", byDepartment);
        result.put("byRole", byRole);

        return ResponseEntity.ok(result);
    }
}
