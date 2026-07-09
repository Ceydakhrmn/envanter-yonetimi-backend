package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_maintenance_schedule")
@Data
public class AssetMaintenanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    // PENDING, COMPLETED, OVERDUE
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
