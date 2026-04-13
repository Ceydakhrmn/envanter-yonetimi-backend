package com.example.demo.repository;

import com.example.demo.entity.AssetAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAssignmentHistoryRepository extends JpaRepository<AssetAssignmentHistory, Long> {
    List<AssetAssignmentHistory> findByAssetIdOrderByCreatedAtDesc(Long assetId);
    List<AssetAssignmentHistory> findByToUserIdOrFromUserIdOrderByCreatedAtDesc(Long toUserId, Long fromUserId);
}
