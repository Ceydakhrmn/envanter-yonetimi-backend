package com.example.demo.repository;

import com.example.demo.entity.AssetAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAttachmentRepository extends JpaRepository<AssetAttachment, Long> {
    List<AssetAttachment> findByAssetIdOrderByCreatedAtDesc(Long assetId);
}
