package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.ActivityLog;
import com.example.demo.entity.AssetMaintenanceRecord;
import com.example.demo.entity.AssetStatusHistory;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.AssetMaintenanceRecordRepository;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.AssetStatusHistoryRepository;
import com.example.demo.repository.KullaniciRepository;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final ActivityLogRepository activityLogRepository;
    private final AssetStatusHistoryRepository statusHistoryRepository;
    private final AssetMaintenanceRecordRepository maintenanceRecordRepository;

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

        long assigned = all.stream().filter(a -> a.getAssignedUser() != null).count();
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

    @Operation(summary = "Audit report", description = "Returns asset activity and status changes for a date range")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> getAuditReport(
            @RequestParam String from,
            @RequestParam String to) {

        LocalDateTime fromDt = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDt = LocalDate.parse(to).atTime(LocalTime.MAX);

        List<ActivityLog> activityLogs = activityLogRepository
                .findByEntityTypeAndCreatedAtBetweenOrderByCreatedAtDesc("ASSET", fromDt, toDt);

        List<AssetStatusHistory> statusChanges = statusHistoryRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(fromDt, toDt);

        // Build unified timeline entries
        List<Map<String, Object>> entries = new ArrayList<>();

        for (ActivityLog log : activityLogs) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "ACTIVITY");
            entry.put("action", log.getAction());
            entry.put("assetId", log.getEntityId());
            entry.put("details", log.getDetails());
            entry.put("performedBy", log.getUserEmail());
            entry.put("createdAt", log.getCreatedAt().toString());
            entries.add(entry);
        }

        for (AssetStatusHistory sh : statusChanges) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "STATUS_CHANGE");
            entry.put("action", "STATUS_CHANGE");
            entry.put("assetId", sh.getAssetId());
            entry.put("assetName", sh.getAssetName());
            entry.put("fromStatus", sh.getFromStatus());
            entry.put("toStatus", sh.getToStatus());
            entry.put("performedBy", sh.getChangedBy());
            entry.put("createdAt", sh.getCreatedAt().toString());
            entries.add(entry);
        }

        entries.sort(Comparator.comparing(e -> (String) e.get("createdAt"), Comparator.reverseOrder()));

        // Summary counts
        long creates = activityLogs.stream().filter(l -> "CREATE".equals(l.getAction())).count();
        long updates = activityLogs.stream().filter(l -> "UPDATE".equals(l.getAction())).count();
        long deletes = activityLogs.stream().filter(l -> "DELETE".equals(l.getAction())).count();
        long assignments = activityLogs.stream()
                .filter(l -> l.getAction() != null && (l.getAction().contains("ASSIGN") || l.getAction().contains("TRANSFER")))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalChanges", entries.size());
        summary.put("creates", creates);
        summary.put("updates", updates);
        summary.put("deletes", deletes);
        summary.put("assignments", assignments);
        summary.put("statusChanges", statusChanges.size());

        Map<String, Object> result = new HashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("summary", summary);
        result.put("entries", entries);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Maintenance summary", description = "Returns maintenance cost and count aggregated by month and asset")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @GetMapping("/maintenance-summary")
    public ResponseEntity<Map<String, Object>> getMaintenanceSummary() {
        LocalDate from = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate to = LocalDate.now();

        List<AssetMaintenanceRecord> records = maintenanceRecordRepository
                .findByMaintenanceDateBetweenOrderByMaintenanceDateAsc(from, to);

        // Monthly aggregation
        Map<String, double[]> byMonth = new java.util.LinkedHashMap<>();
        // Pre-fill last 12 months so chart has no gaps
        for (int i = 11; i >= 0; i--) {
            String key = LocalDate.now().minusMonths(i).getYear() + "-"
                    + String.format("%02d", LocalDate.now().minusMonths(i).getMonthValue());
            byMonth.put(key, new double[]{0, 0}); // [cost, count]
        }
        for (AssetMaintenanceRecord r : records) {
            String key = r.getMaintenanceDate().getYear() + "-"
                    + String.format("%02d", r.getMaintenanceDate().getMonthValue());
            byMonth.computeIfAbsent(key, k -> new double[]{0, 0});
            if (r.getCost() != null) byMonth.get(key)[0] += r.getCost().doubleValue();
            byMonth.get(key)[1]++;
        }
        List<Map<String, Object>> monthly = new ArrayList<>();
        byMonth.forEach((month, vals) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("month", month);
            row.put("totalCost", Math.round(vals[0] * 100.0) / 100.0);
            row.put("count", (long) vals[1]);
            monthly.add(row);
        });

        // Per-asset aggregation (top 10 by cost)
        Map<Long, double[]> assetMap = new HashMap<>();
        Map<Long, String> assetNames = new HashMap<>();
        for (AssetMaintenanceRecord r : records) {
            assetMap.computeIfAbsent(r.getAssetId(), k -> new double[]{0, 0});
            if (r.getCost() != null) assetMap.get(r.getAssetId())[0] += r.getCost().doubleValue();
            assetMap.get(r.getAssetId())[1]++;
            assetNames.put(r.getAssetId(), r.getAssetName());
        }
        List<Map<String, Object>> byAsset = assetMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("assetId", e.getKey());
                    row.put("assetName", assetNames.get(e.getKey()));
                    row.put("totalCost", Math.round(e.getValue()[0] * 100.0) / 100.0);
                    row.put("count", (long) e.getValue()[1]);
                    return row;
                }).toList();

        // Totals
        double totalCost = records.stream()
                .filter(r -> r.getCost() != null)
                .mapToDouble(r -> r.getCost().doubleValue()).sum();
        long totalCount = records.size();

        Map<String, Object> totals = new HashMap<>();
        totals.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        totals.put("totalCount", totalCount);
        totals.put("avgCost", totalCount > 0 ? Math.round((totalCost / totalCount) * 100.0) / 100.0 : 0);

        Map<String, Object> result = new HashMap<>();
        result.put("monthly", monthly);
        result.put("byAsset", byAsset);
        result.put("totals", totals);

        return ResponseEntity.ok(result);
    }
}
