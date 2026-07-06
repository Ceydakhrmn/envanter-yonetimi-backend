package com.example.demo.repository;

import com.example.demo.entity.AssetMaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetMaintenanceRecordRepository extends JpaRepository<AssetMaintenanceRecord, Long> {
    List<AssetMaintenanceRecord> findByAssetIdOrderByMaintenanceDateDesc(Long assetId);
}
