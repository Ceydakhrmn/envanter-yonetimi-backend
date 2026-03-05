package com.example.demo.repository;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    void deleteByKullanici(Kullanici kullanici);
    
    Optional<RefreshToken> findByKullanici(Kullanici kullanici);
}
