package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "kullanicilar")
@Data
public class Kullanici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;
    private String password;

    @Column(name = "department")
    private String department;

    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    @Column(name = "aktif")
    private Boolean aktif = true;
}
