package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Notification> findTop50ByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Notification> findByUserEmailAndReadFalseOrderByCreatedAtDesc(String userEmail);

    long countByUserEmailAndReadFalse(String userEmail);

    void deleteByUserEmail(String userEmail);
}
