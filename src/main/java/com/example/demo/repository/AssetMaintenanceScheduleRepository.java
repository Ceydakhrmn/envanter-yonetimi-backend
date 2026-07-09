package com.example.demo.repository;

import com.example.demo.entity.AssetMaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssetMaintenanceScheduleRepository extends JpaRepository<AssetMaintenanceSchedule, Long> {
    List<AssetMaintenanceSchedule> findByAssetIdOrderByScheduledDateAsc(Long assetId);
    List<AssetMaintenanceSchedule> findByStatusAndScheduledDateBefore(String status, LocalDate date);
    List<AssetMaintenanceSchedule> findByStatusAndScheduledDateBetween(String status, LocalDate from, LocalDate to);
}
