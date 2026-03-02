package com.example.demo.entity;

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
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad boş olamaz")
    @Size(min = 2, max = 50, message = "Ad 2-50 karakter arası olmalı")
    @Column(nullable = false, length = 50)
    private String ad;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 karakter arası olmalı")
    @Column(nullable = false, length = 50)
    private String soyad;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Departman boş olamaz")
    @Column(nullable = false, length = 50)
    private String departman;

    @Column(name = "kayit_tarihi", nullable = false, updatable = false)
    private LocalDateTime kayitTarihi;

    @Column(name = "aktif", nullable = false)
    private Boolean aktif;

    @PrePersist  // Kayıt edilmeden önce çalışır
    protected void onCreate() {
        kayitTarihi = LocalDateTime.now();
        if (aktif == null) {
            aktif = true;  // Varsayılan olarak aktif
        }
    }
}
