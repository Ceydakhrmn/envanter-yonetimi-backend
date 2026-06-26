package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final AssetRepository assetRepository;
    private final KullaniciRepository kullaniciRepository;

    @GetMapping("/department-summary")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentSummary() {
        List<Asset> allAssets = assetRepository.findAll();
        List<Kullanici> allUsers = kullaniciRepository.findAll();

        Map<String, List<Asset>> assetsByDept = allAssets.stream()
                .filter(a -> a.getAssignedDepartment() != null && !a.getAssignedDepartment().isBlank())
                .collect(Collectors.groupingBy(Asset::getAssignedDepartment));

        Map<String, List<Kullanici>> usersByDept = allUsers.stream()
                .filter(u -> u.getDepartment() != null && !u.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Kullanici::getDepartment));

        Set<String> allDepts = new TreeSet<>();
        allDepts.addAll(assetsByDept.keySet());
        allDepts.addAll(usersByDept.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (String dept : allDepts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", dept);

            List<Kullanici> deptUsers = usersByDept.getOrDefault(dept, List.of());
            row.put("totalUsers", deptUsers.size());
            row.put("activeUsers", deptUsers.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count());

            List<Asset> deptAssets = assetsByDept.getOrDefault(dept, List.of());
            row.put("totalAssets", deptAssets.size());
            row.put("activeAssets", deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.ACTIVE).count());
            row.put("maintenanceAssets", deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.MAINTENANCE).count());
            row.put("expiredAssets", deptAssets.stream().filter(a -> a.getStatus() == Asset.Status.EXPIRED).count());

            BigDecimal totalValue = deptAssets.stream()
                    .filter(a -> a.getPurchasePrice() != null)
                    .map(Asset::getPurchasePrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("totalValue", totalValue);

            Map<String, Long> byCategory = deptAssets.stream()
                    .collect(Collectors.groupingBy(a -> a.getCategory().name(), Collectors.counting()));
            row.put("byCategory", byCategory);

            result.add(row);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/asset-overview")
    public ResponseEntity<Map<String, Object>> getAssetOverview() {
        List<Asset> all = assetRepository.findAll();
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("total", all.size());

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        result.put("byStatus", byStatus);

        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(a -> a.getCategory().name(), Collectors.counting()));
        result.put("byCategory", byCategory);

        long assigned = all.stream().filter(a -> a.getAssignedUser() != null).count();
        result.put("assigned", assigned);
        result.put("unassigned", all.size() - assigned);

        long expiringSoon = all.stream()
                .filter(a -> a.getRenewalDate() != null && a.getStatus() == Asset.Status.ACTIVE
                        && a.getRenewalDate().isBefore(LocalDate.now().plusDays(30)))
                .count();
        result.put("expiringSoon", expiringSoon);

        BigDecimal totalValue = all.stream()
                .filter(a -> a.getPurchasePrice() != null)
                .map(Asset::getPurchasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalValue", totalValue);

        // Monthly acquisition trend (last 12 months)
        LocalDate twelveMonthsAgo = LocalDate.now().minusMonths(12);
        Map<String, Long> monthlyAcquisition = all.stream()
                .filter(a -> a.getPurchaseDate() != null && a.getPurchaseDate().isAfter(twelveMonthsAgo))
                .collect(Collectors.groupingBy(
                        a -> a.getPurchaseDate().getYear() + "-" + String.format("%02d", a.getPurchaseDate().getMonthValue()),
                        Collectors.counting()));
        result.put("monthlyAcquisition", monthlyAcquisition);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-overview")
    public ResponseEntity<Map<String, Object>> getUserOverview() {
        List<Kullanici> all = kullaniciRepository.findAll();
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("total", all.size());
        result.put("active", all.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count());
        result.put("inactive", all.stream().filter(u -> !Boolean.TRUE.equals(u.getActive())).count());

        Map<String, Long> byDepartment = all.stream()
                .filter(u -> u.getDepartment() != null && !u.getDepartment().isBlank())
                .collect(Collectors.groupingBy(Kullanici::getDepartment, Collectors.counting()));
        result.put("byDepartment", byDepartment);

        Map<String, Long> byRole = all.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));
        result.put("byRole", byRole);

        return ResponseEntity.ok(result);
    }
}
