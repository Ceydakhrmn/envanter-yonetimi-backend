package com.example.demo.repository;

import com.example.demo.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

    List<ActivityLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
}
