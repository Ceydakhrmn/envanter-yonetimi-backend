package com.example.demo.repository;

import com.example.demo.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Manages database operations
 * CRUD operations are provided by JpaRepository
 */
@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    // Spring Data JPA automatically implements these methods

    // Find user by email
    Optional<Kullanici> findByEmail(String email);

    // List users by department
    List<Kullanici> findByDepartment(String department);

    // List active users
    List<Kullanici> findByActiveTrue();

    // Email existence check
    boolean existsByEmail(String email);

    // Bulk delete by IDs
    void deleteAllByIdIn(List<Long> ids);
}
