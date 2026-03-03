package com.example.demo.repository;

import com.example.demo.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Kullanıcı Repository - Veritabanı işlemlerini yönetir
 * JpaRepository sayesinde CRUD işlemleri otomatik gelir
 */
@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    // Spring Data JPA otomatik olarak bu metotları implement eder
    
    // Email ile kullanıcı bulma
    Optional<Kullanici> findByEmail(String email);
    
    // Departmana göre kullanıcıları listeleme
    List<Kullanici> findByDepartment(String department);
    
    // Aktif kullanıcıları listeleme
    List<Kullanici> findByAktifTrue();
    
    // Email varlık kontrolü
    boolean existsByEmail(String email);
}
