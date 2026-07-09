package com.example.demo.repository;

import com.example.demo.entity.LicenseSeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseSeatAssignmentRepository extends JpaRepository<LicenseSeatAssignment, Long> {
    List<LicenseSeatAssignment> findByAssetIdOrderByAssignedAtAsc(Long assetId);
    boolean existsByAssetIdAndUserId(Long assetId, Long userId);
    long countByAssetId(Long assetId);
}
