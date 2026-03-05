package com.example.demo.service;

import com.example.demo.config.JwtProperties;
import com.example.demo.entity.Kullanici;
import com.example.demo.entity.RefreshToken;
import com.example.demo.exception.RefreshTokenExpiredException;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Refresh Token Service Tests")
class RefreshTokenServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Should return token when not expired")
    void verifyExpiration_shouldReturnToken_whenNotExpired() {
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(300))
                .build();

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isSameAs(token);
        verify(refreshTokenRepository, never()).delete(token);
    }

    @Test
    @DisplayName("Should delete and throw when token expired")
    void verifyExpiration_shouldDeleteAndThrow_whenExpired() {
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(1))
                .build();

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessageContaining("süresi dolmuş");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    @DisplayName("Should find token by value")
    void findByToken_shouldReturnToken_whenExists() {
        RefreshToken token = RefreshToken.builder()
                .token("abc")
                .expiryDate(Instant.now().plusSeconds(120))
                .build();

        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("abc");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("abc");
    }

    @Test
    @DisplayName("Should delete refresh token by user id")
    void deleteByKullaniciId_shouldDelete_whenUserExists() {
        Kullanici kullanici = new Kullanici();
        kullanici.setId(1L);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(kullanici));

        refreshTokenService.deleteByKullaniciId(1L);

        verify(refreshTokenRepository).deleteByKullanici(kullanici);
    }
}
