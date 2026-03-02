package com.example.demo.service;

import com.example.demo.controller.BaseController;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service - Business logic is here
 * Bridge between Repository and Controller
 */
@Service
@RequiredArgsConstructor  // Lombok: Creates constructor for final fields
@Slf4j  // Lombok: Automatically creates Logger (you can use log.info(...))
public class KullaniciService implements BaseController.BaseService<Kullanici, Long> {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== BaseService Interface Implementation ====================
    
    @Override
    public List<Kullanici> findAll() {
        return tumKullanicilar();
    }

    @Override
    public Kullanici findById(Long id) {
        return kullaniciBul(id);
    }

    @Override
    @Transactional
    public Kullanici create(Kullanici entity) {
        return kullaniciOlustur(entity);
    }

    @Override
    @Transactional
    public Kullanici update(Long id, Kullanici entity) {
        return kullaniciGuncelle(id, entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        kullaniciSil(id);
    }

    // ==================== Original Methods (kept for backward compatibility) ====================

    /**
     * Create new user
     */
    @Transactional
    public Kullanici kullaniciOlustur(Kullanici kullanici) {
        log.info("Creating new user: {}", kullanici.getEmail());
        
        // Email check
        if (kullaniciRepository.existsByEmail(kullanici.getEmail())) {
            throw new RuntimeException("This email address is already registered: " + kullanici.getEmail());
        }
        
        return kullaniciRepository.save(kullanici);
    }

    /**
     * List all users
     */
    public List<Kullanici> tumKullanicilar() {
        log.info("Listing all users");
        return kullaniciRepository.findAll();
    }

    /**
     * Find user by ID
     */
    public Kullanici kullaniciBul(Long id) {
        log.info("Searching for user: ID={}", id);
        return kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: ID=" + id));
    }

    /**
     * Find user by email
     */
    public Kullanici emailIleKullaniciBul(String email) {
        log.info("Searching for user: Email={}", email);
        return kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: Email=" + email));
    }

    /**
     * List users by department
     */
    public List<Kullanici> departmanaGoreListele(String departman) {
        log.info("Listing by department: {}", departman);
        return kullaniciRepository.findByDepartman(departman);
    }

    /**
     * Update user
     */
    @Transactional
    public Kullanici kullaniciGuncelle(Long id, Kullanici yeniKullanici) {
        log.info("Updating user: ID={}", id);
        
        Kullanici mevcutKullanici = kullaniciBul(id);
        
        mevcutKullanici.setAd(yeniKullanici.getAd());
        mevcutKullanici.setSoyad(yeniKullanici.getSoyad());
        mevcutKullanici.setDepartman(yeniKullanici.getDepartman());
        
        // Update active status (if provided)
        if (yeniKullanici.getAktif() != null) {
            mevcutKullanici.setAktif(yeniKullanici.getAktif());
        }
        
        // If email is changing, check for duplicate
        if (!mevcutKullanici.getEmail().equals(yeniKullanici.getEmail())) {
            if (kullaniciRepository.existsByEmail(yeniKullanici.getEmail())) {
                throw new RuntimeException("This email address is already in use: " + yeniKullanici.getEmail());
            }
            mevcutKullanici.setEmail(yeniKullanici.getEmail());
        }
        
        return kullaniciRepository.save(mevcutKullanici);
    }

    /**
     * Delete user (soft delete - active=false)
     */
    @Transactional
    public void kullaniciSil(Long id) {
        log.info("Deleting user (deactivating): ID={}", id);
        Kullanici kullanici = kullaniciBul(id);
        kullanici.setAktif(false);
        kullaniciRepository.save(kullanici);
    }

    /**
     * Permanently delete user (hard delete - completely remove from database)
     */
    @Transactional
    public void kullaniciKaliciSil(Long id) {
        log.warn("Permanently deleting user: ID={}", id);
        kullaniciRepository.deleteById(id);
    }

    /**
     * List active users
     */
    public List<Kullanici> aktifKullanicilar() {
        log.info("Listing active users");
        return kullaniciRepository.findByAktifTrue();
    }

    /**
     * Login - Verify email and password
     */
    public boolean login(String email, String password) {
        log.info("Login attempt: {}", email);
        
        // Find user by email
        var kullaniciOpt = kullaniciRepository.findByEmail(email);
        if (kullaniciOpt.isEmpty()) {
            log.warn("Login failed: User not found - {}", email);
            return false;
        }
        
        Kullanici kullanici = kullaniciOpt.get();
        
        // Check if user is active
        if (!kullanici.getAktif()) {
            log.warn("Login failed: User is inactive - {}", email);
            return false;
        }
        
        // Verify password
        boolean passwordMatches = passwordEncoder.matches(password, kullanici.getPassword());
        if (passwordMatches) {
            log.info("Login successful: {}", email);
        } else {
            log.warn("Login failed: Invalid password - {}", email);
        }
        
        return passwordMatches;
    }
}
