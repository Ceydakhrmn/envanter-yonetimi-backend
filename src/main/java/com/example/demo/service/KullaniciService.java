package com.example.demo.service;

import com.example.demo.controller.BaseController;
import com.example.demo.entity.Kullanici;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.SamePasswordException;
import com.example.demo.exception.UserNotFoundException;
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
            throw new EmailAlreadyExistsException("This email address is already registered: " + kullanici.getEmail());
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
            .orElseThrow(() -> new UserNotFoundException("User not found: ID=" + id));
    }

    /**
     * Find user by email
     */
    public Kullanici emailIleKullaniciBul(String email) {
        log.info("Searching for user: Email={}", email);
        return kullaniciRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found: Email=" + email));
    }

    /**
     * List users by department
     */
    public List<Kullanici> departmanaGoreListele(String department) {
        log.info("Listing by department: {}", department);
        return kullaniciRepository.findByDepartment(department);
    }

    /**
     * Update user
     */
    @Transactional
    public Kullanici kullaniciGuncelle(Long id, Kullanici yeniKullanici) {
        log.info("Updating user: ID={}", id);
        Kullanici mevcutKullanici = kullaniciBul(id);
        mevcutKullanici.setFirstName(yeniKullanici.getFirstName());
        mevcutKullanici.setLastName(yeniKullanici.getLastName());
        mevcutKullanici.setDepartment(yeniKullanici.getDepartment());
        // Update active status (if provided)
        if (yeniKullanici.getActive() != null) {
            mevcutKullanici.setActive(yeniKullanici.getActive());
        }
        // If email is changing, check for duplicate
        if (!mevcutKullanici.getEmail().equals(yeniKullanici.getEmail())) {
            if (kullaniciRepository.existsByEmail(yeniKullanici.getEmail())) {
                throw new EmailAlreadyExistsException("This email address is already in use: " + yeniKullanici.getEmail());
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
        kullanici.setActive(false);
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
    public List<Kullanici> activeUsers() {
        log.info("Listing active users");
        return kullaniciRepository.findByActiveTrue();
    }

    /**
     * Login - Verify email and password and return Kullanici when successful
     */
    public Kullanici login(String email, String password) {
        log.info("Login attempt: {}", email);
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!kullanici.getActive()) {
            log.warn("Login failed: User is inactive - {}", email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        boolean passwordMatches = passwordEncoder.matches(password, kullanici.getPassword());
        if (passwordMatches) {
            log.info("Login successful: {}", email);
            return kullanici;
        }

        log.warn("Login failed: Invalid password - {}", email);
        throw new InvalidCredentialsException("Invalid email or password");
    }

    /**
     * Change password for the authenticated user.
     */
    @Transactional
    public void sifreDegistir(String email, String currentPassword, String newPassword) {
        log.info("Password change attempt: {}", email);

        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: Email=" + email));

        if (!passwordEncoder.matches(currentPassword, kullanici.getPassword())) {
            log.warn("Password change failed: current password mismatch - {}", email);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (currentPassword.equals(newPassword)) {
            log.warn("Password change failed: new password is same as current - {}", email);
            throw new SamePasswordException("New password must be different from current password");
        }

        kullanici.setPassword(passwordEncoder.encode(newPassword));
        kullaniciRepository.save(kullanici);
        log.info("Password changed successfully: {}", email);
    }
}
