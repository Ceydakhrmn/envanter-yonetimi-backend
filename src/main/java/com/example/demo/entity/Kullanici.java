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
@Schema(description = "Kullanıcı entity modeli")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Kullanıcı ID (otomatik oluşturulur)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Ad boş olamaz")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arası olmalı")
    @Column(nullable = false, length = 50)
    @Schema(description = "Kullanıcının adı", example = "Ahmet", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arası olmalı")
    @Column(nullable = false, length = 50)
    @Schema(description = "Kullanıcının soyadı", example = "Yılmaz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String soyad;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Kullanıcının email adresi (unique)", example = "ahmet@efsora.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Departman boş olamaz")
    @Column(nullable = false, length = 50)
    @Schema(description = "Kullanıcının departmanı", example = "IT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departman;

    @Column(name = "kayit_tarihi", nullable = false, updatable = false)
    @Schema(description = "Kayıt tarihi (otomatik oluşturulur)", example = "2026-03-02T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime kayitTarihi;

    @Column(name = "aktif", nullable = false)
    @Schema(description = "Kullanıcı aktif mi? (false = silinmiş)", example = "true", defaultValue = "true")
    private Boolean aktif;

    @PrePersist  // Kayıt edilmeden önce çalışır
    protected void onCreate() {
        kayitTarihi = LocalDateTime.now();
        if (aktif == null) {
            aktif = true;  // Varsayılan olarak aktif
        }
    }
}
