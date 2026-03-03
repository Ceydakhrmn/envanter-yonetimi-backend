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


    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "active")
    private Boolean active = true;
}
