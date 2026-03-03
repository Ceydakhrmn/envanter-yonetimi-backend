
package com.example.demo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "kullanicilar")
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

    @Column(name = "password", nullable = false)
    @Schema(description = "User's password (hashed)", example = "$2a$10$...", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column(name = "kayit_tarihi", nullable = false, updatable = false)
    @Schema(description = "Registration date (auto-generated)", example = "2026-03-02T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime kayitTarihi;

    @Column(name = "aktif", nullable = false)
    @Schema(description = "Is user active? (false = deleted)", example = "true", defaultValue = "true")
    private Boolean aktif;

    public Kullanici() {}

    public Kullanici(Long id, String ad, String soyad, String email, String departman, String password, LocalDateTime kayitTarihi, Boolean aktif) {
        this.id = id;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.departman = departman;
        this.password = password;
        this.kayitTarihi = kayitTarihi;
        this.aktif = aktif;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartman() { return departman; }
    public void setDepartman(String departman) { this.departman = departman; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getKayitTarihi() { return kayitTarihi; }
    public void setKayitTarihi(LocalDateTime kayitTarihi) { this.kayitTarihi = kayitTarihi; }

    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }

    @PrePersist
    protected void onCreate() {
        kayitTarihi = LocalDateTime.now();
        if (aktif == null) {
            aktif = true;
        }
    }
}
