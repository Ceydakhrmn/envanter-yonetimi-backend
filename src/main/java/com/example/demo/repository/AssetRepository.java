package com.example.demo.repository;

import com.example.demo.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByCategory(Asset.Category category);

    List<Asset> findByStatus(Asset.Status status);

    List<Asset> findByAssignedDepartment(String department);

    List<Asset> findByAssignedUserId(Long userId);

    @Query("SELECT a FROM Asset a WHERE a.renewalDate IS NOT NULL AND a.renewalDate <= :date AND a.status = 'ACTIVE'")
    List<Asset> findExpiringBefore(LocalDate date);

    @Query("SELECT a FROM Asset a WHERE a.renewalDate IS NOT NULL AND a.renewalDate BETWEEN :from AND :to AND a.status = 'ACTIVE'")
    List<Asset> findByRenewalDateBetween(@org.springframework.data.repository.query.Param("from") LocalDate from, @org.springframework.data.repository.query.Param("to") LocalDate to);

    @Query("SELECT a FROM Asset a WHERE a.warrantyExpiryDate IS NOT NULL AND a.warrantyExpiryDate BETWEEN :from AND :to")
    List<Asset> findByWarrantyExpiryDateBetween(@org.springframework.data.repository.query.Param("from") LocalDate from, @org.springframework.data.repository.query.Param("to") LocalDate to);

    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.brand) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Asset> search(String q);

    @Query("SELECT a FROM Asset a JOIN a.tags t WHERE LOWER(t) = LOWER(:tag)")
    List<Asset> findByTag(@org.springframework.data.repository.query.Param("tag") String tag);

    @Query("SELECT DISTINCT t FROM Asset a JOIN a.tags t ORDER BY t")
    List<String> findAllTags();
}
