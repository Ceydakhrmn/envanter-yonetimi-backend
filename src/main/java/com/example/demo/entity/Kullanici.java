package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User Entity - Implements UserDetails for Spring Security
 * Represents a user in the system with authentication capabilities
 */
@Entity
@Table(name = "kullanicilar")
@Data
public class Kullanici implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "department")
    private String department;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;


    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "profile_photo", columnDefinition = "TEXT")
    private String profilePhoto;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // MFA/2FA fields
    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "mfa_type")
    @Enumerated(EnumType.STRING)
    private MfaType mfaType = MfaType.NONE;

    @Column(name = "mfa_backup_codes", columnDefinition = "TEXT")
    private String mfaBackupCodes; // CSV string, hashed

    public enum MfaType {
        NONE, TOTP, EMAIL, BACKUP
    }

    public enum Role {
        ADMIN, USER, EDITOR
    }

    // ==================== UserDetails Implementation ====================

    /**
     * Returns the authorities (roles) granted to the user
     * Now returns the user's actual role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role.name() : "USER")));
    }

    /**
     * Returns the username used to authenticate the user
     * We use email as username
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the password used to authenticate the user
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Indicates whether the user's account has expired
     * We always return true (account never expires)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked
     * We always return true (account never locked)
     */
    @Override
    public boolean isAccountNonLocked() {
        if (lockExpiresAt == null) return true;
        if (lockExpiresAt.isBefore(LocalDateTime.now())) {
            // Kilit süresi doldu, otomatik aç
            lockExpiresAt = null;
            failedLoginAttempts = 0;
            return true;
        }
        return false;
    }

    /**
     * Indicates whether the user's credentials (password) has expired
     * We always return true (credentials never expire)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled
     * We return the 'active' field value
     */
    @Override
    public boolean isEnabled() {
        return active != null && active;
    }
}
