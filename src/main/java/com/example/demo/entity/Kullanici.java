package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kullanıcı Entity - Veritabanındaki 'kullanicilar' tablosunu temsil eder
 */
@Entity
@Table(name = "kullanicilar")
@Data  // Lombok: getter, setter, toString, equals, hashCode otomatik
@NoArgsConstructor  // Lombok: Parametresiz constructor
@AllArgsConstructor  // Lombok: Tüm parametrelerle constructor
@Schema(description = "User entity model")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID (auto-generated)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
    @Column(nullable = false, length = 50)
    @Schema(description = "User's first name", example = "Ahmet", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ad;

    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name must be between 2-50 characters")
    @Column(nullable = false, length = 50)
    @Schema(description = "User's last name", example = "Yılmaz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String soyad;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "User's email address (unique)", example = "ahmet@efsora.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Department cannot be empty")
    @Column(nullable = false, length = 50)
    @Schema(description = "User's department", example = "IT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departman;

    @Column(name = "kayit_tarihi", nullable = false, updatable = false)
    @Schema(description = "Registration date (auto-generated)", example = "2026-03-02T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime kayitTarihi;

    @Column(name = "aktif", nullable = false)
    @Schema(description = "Is user active? (false = deleted)", example = "true", defaultValue = "true")
    private Boolean aktif;

    @PrePersist  // Kayıt edilmeden önce çalışır
    protected void onCreate() {
        kayitTarihi = LocalDateTime.now();
        if (aktif == null) {
            aktif = true;  // Varsayılan olarak aktif
        }
    }
}
