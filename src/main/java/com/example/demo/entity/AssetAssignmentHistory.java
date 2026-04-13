package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_assignment_history")
@Data
public class AssetAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "action", nullable = false)
    private String action; // ASSIGNED, UNASSIGNED, REASSIGNED

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "from_user_name")
    private String fromUserName;

    @Column(name = "to_user_id")
    private Long toUserId;

    @Column(name = "to_user_name")
    private String toUserName;

    @Column(name = "from_department")
    private String fromDepartment;

    @Column(name = "to_department")
    private String toDepartment;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
