package com.example.demo.repository;

import com.example.demo.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

    List<ActivityLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    // Güvenlik istatistikleri
    long countByActionAndCreatedAtAfter(String action, LocalDateTime after);

    @Query("SELECT DISTINCT a.ipAddress FROM ActivityLog a WHERE a.action = 'LOGIN_FAILED' AND a.createdAt > :since AND a.ipAddress IS NOT NULL")
    List<String> findSuspiciousIpsSince(@Param("since") LocalDateTime since);

    List<ActivityLog> findByActionInAndCreatedAtAfterOrderByCreatedAtDesc(List<String> actions, LocalDateTime after);
}
