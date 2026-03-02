package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * KullaniciService Unit Tests
 * Using Mockito to mock the repository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class KullaniciServiceTest {

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private KullaniciService kullaniciService;

    private Kullanici testKullanici;

    @BeforeEach
    void setUp() {
        testKullanici = new Kullanici();
        testKullanici.setId(1L);
        testKullanici.setAd("Ahmet");
        testKullanici.setSoyad("Yılmaz");
        testKullanici.setEmail("ahmet@efsora.com");
        testKullanici.setDepartman("IT");
        testKullanici.setAktif(true);
    }

    @Test
    @DisplayName("User should be created successfully")
    void kullaniciOlustur_basarili() {
        // Given - Setup
        when(kullaniciRepository.existsByEmail(anyString())).thenReturn(false);
        when(kullaniciRepository.save(any(Kullanici.class))).thenReturn(testKullanici);

        // When - Action
        Kullanici sonuc = kullaniciService.kullaniciOlustur(testKullanici);

        // Then - Verification
        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getEmail()).isEqualTo("ahmet@efsora.com");
        assertThat(sonuc.getAd()).isEqualTo("Ahmet");
        
        verify(kullaniciRepository, times(1)).existsByEmail("ahmet@efsora.com");
        verify(kullaniciRepository, times(1)).save(testKullanici);
    }

    @Test
    @DisplayName("Second user with same email should not be created")
    void kullaniciOlustur_emailZatenVar() {
        // Given
        when(kullaniciRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> kullaniciService.kullaniciOlustur(testKullanici))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("This email address is already");

        verify(kullaniciRepository, never()).save(any(Kullanici.class));
    }

    @Test
    @DisplayName("All users should be listed")
    void tumKullanicilar_basarili() {
        // Given
        Kullanici kullanici2 = new Kullanici();
        kullanici2.setId(2L);
        kullanici2.setEmail("mehmet@efsora.com");
        
        List<Kullanici> kullaniciListesi = Arrays.asList(testKullanici, kullanici2);
        when(kullaniciRepository.findAll()).thenReturn(kullaniciListesi);

        // When
        List<Kullanici> sonuc = kullaniciService.tumKullanicilar();

        // Then
        assertThat(sonuc).hasSize(2);
        assertThat(sonuc).contains(testKullanici, kullanici2);
        verify(kullaniciRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("User should be found by ID")
    void kullaniciBul_basarili() {
        // Given
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(testKullanici));

        // When
        Kullanici sonuc = kullaniciService.kullaniciBul(1L);

        // Then
        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getId()).isEqualTo(1L);
        assertThat(sonuc.getEmail()).isEqualTo("ahmet@efsora.com");
    }

    @Test
    @DisplayName("Should throw error when user not found by ID")
    void kullaniciBul_bulunamadi() {
        // Given
        when(kullaniciRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> kullaniciService.kullaniciBul(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("User should be found by email")
    void emailIleKullaniciBul_basarili() {
        // Given
        when(kullaniciRepository.findByEmail("ahmet@efsora.com")).thenReturn(Optional.of(testKullanici));

        // When
        Kullanici sonuc = kullaniciService.emailIleKullaniciBul("ahmet@efsora.com");

        // Then
        assertThat(sonuc).isNotNull();
        assertThat(sonuc.getEmail()).isEqualTo("ahmet@efsora.com");
    }

    @Test
    @DisplayName("User should be updated")
    void kullaniciGuncelle_basarili() {
        // Given
        Kullanici guncellenenKullanici = new Kullanici();
        guncellenenKullanici.setAd("Ahmet");
        guncellenenKullanici.setSoyad("Kaya"); // Last name changed
        guncellenenKullanici.setEmail("ahmet@efsora.com");
        guncellenenKullanici.setDepartman("HR"); // Department changed
        guncellenenKullanici.setAktif(true);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(testKullanici));
        when(kullaniciRepository.save(any(Kullanici.class))).thenReturn(testKullanici);

        // When
        Kullanici sonuc = kullaniciService.kullaniciGuncelle(1L, guncellenenKullanici);

        // Then
        assertThat(sonuc).isNotNull();
        verify(kullaniciRepository, times(1)).save(any(Kullanici.class));
    }

    @Test
    @DisplayName("User should be deleted with soft delete")
    void kullaniciSil_softDelete() {
        // Given
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(testKullanici));

        // When
        kullaniciService.kullaniciSil(1L);

        // Then
        assertThat(testKullanici.getAktif()).isFalse();
        verify(kullaniciRepository, times(1)).save(testKullanici);
    }

    @Test
    @DisplayName("Active users should be listed")
    void aktifKullanicilar_basarili() {
        // Given
        List<Kullanici> aktifKullanicilar = Arrays.asList(testKullanici);
        when(kullaniciRepository.findByAktifTrue()).thenReturn(aktifKullanicilar);

        // When
        List<Kullanici> sonuc = kullaniciService.aktifKullanicilar();

        // Then
        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).getAktif()).isTrue();
    }

    @Test
    @DisplayName("Users should be listed by department")
    void departmanaGoreListele_basarili() {
        // Given
        List<Kullanici> itKullanicilari = Arrays.asList(testKullanici);
        when(kullaniciRepository.findByDepartman("IT")).thenReturn(itKullanicilari);

        // When
        List<Kullanici> sonuc = kullaniciService.departmanaGoreListele("IT");

        // Then
        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).getDepartman()).isEqualTo("IT");
    }
}
