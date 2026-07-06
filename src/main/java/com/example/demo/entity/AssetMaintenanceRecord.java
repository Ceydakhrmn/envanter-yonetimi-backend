package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_maintenance_record")
@Data
public class AssetMaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "logged_by")
    private String loggedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
