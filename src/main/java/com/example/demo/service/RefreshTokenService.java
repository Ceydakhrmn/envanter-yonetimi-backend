package com.example.demo.service;

import com.example.demo.config.JwtProperties;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KullaniciRepository kullaniciRepository;

    /**
     * Kullanıcı için yeni bir refresh token oluşturur
     */
    public RefreshToken createRefreshToken(Long kullaniciId) {
        // Önce mevcut token'ı sil (her kullanıcının tek bir refresh token'ı olsun)
        kullaniciRepository.findById(kullaniciId).ifPresent(kullanici -> {
            refreshTokenRepository.findByKullanici(kullanici)
                    .ifPresent(refreshTokenRepository::delete);
        });

        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        RefreshToken refreshToken = RefreshToken.builder()
                .kullanici(kullanici)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Token string'den RefreshToken entity'sini bulur
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Refresh token'ın süresinin dolup dolmadığını kontrol eder
     * Süre dolmuşsa veritabanından siler
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token süresi dolmuş. Lütfen tekrar giriş yapın.");
        }
        return token;
    }

    /**
     * Kullanıcının refresh token'ını siler (logout için)
     */
    @Transactional
    public void deleteByKullaniciId(Long kullaniciId) {
        kullaniciRepository.findById(kullaniciId).ifPresent(kullanici -> {
            refreshTokenRepository.deleteByKullanici(kullanici);
        });
    }
}
