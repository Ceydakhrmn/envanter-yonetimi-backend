package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.SamePasswordException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.KullaniciRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private KullaniciService kullaniciService;

    private Kullanici testKullanici;

    @BeforeEach
    void setUp() {
        testKullanici = new Kullanici();
        testKullanici.setId(1L);
        testKullanici.setFirstName("Ahmet");
        testKullanici.setLastName("Yılmaz");
        testKullanici.setEmail("ahmet@efsora.com");
        testKullanici.setDepartment("IT");
        testKullanici.setActive(true);
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
        assertThat(sonuc.getFirstName()).isEqualTo("Ahmet");
        
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
            .isInstanceOf(EmailAlreadyExistsException.class)
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
            .isInstanceOf(UserNotFoundException.class)
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
        guncellenenKullanici.setFirstName("Ahmet");
        guncellenenKullanici.setLastName("Kaya"); // Last name changed
        guncellenenKullanici.setEmail("ahmet@efsora.com");
        guncellenenKullanici.setDepartment("HR"); // Department changed
        guncellenenKullanici.setActive(true);

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
        assertThat(testKullanici.getActive()).isFalse();
        verify(kullaniciRepository, times(1)).save(testKullanici);
    }

    @Test
    @DisplayName("Active users should be listed")
    void activeUsers_success() {
        // Given
        List<Kullanici> activeUsers = Arrays.asList(testKullanici);
        when(kullaniciRepository.findByActiveTrue()).thenReturn(activeUsers);
        
        // When
        List<Kullanici> result = kullaniciService.activeUsers();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("Users should be listed by department")
    void departmanaGoreListele_basarili() {
        // Given
        List<Kullanici> itKullanicilari = Arrays.asList(testKullanici);
        when(kullaniciRepository.findByDepartment("IT")).thenReturn(itKullanicilari);

        // When
        List<Kullanici> sonuc = kullaniciService.departmanaGoreListele("IT");

        // Then
        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).getDepartment()).isEqualTo("IT");
    }

    @Test
    @DisplayName("Login should succeed when credentials are valid")
    void login_basarili() {
        when(kullaniciRepository.findByEmail("ahmet@efsora.com")).thenReturn(Optional.of(testKullanici));
        when(passwordEncoder.matches("pass123", testKullanici.getPassword())).thenReturn(true);

        Kullanici result = kullaniciService.login("ahmet@efsora.com", "pass123");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ahmet@efsora.com");
    }

    @Test
    @DisplayName("Login should fail when email does not exist")
    void login_emailYok() {
        when(kullaniciRepository.findByEmail("none@efsora.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kullaniciService.login("none@efsora.com", "pass123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Login should fail when user is inactive")
    void login_kullaniciPasif() {
        testKullanici.setActive(false);
        when(kullaniciRepository.findByEmail("ahmet@efsora.com")).thenReturn(Optional.of(testKullanici));

        assertThatThrownBy(() -> kullaniciService.login("ahmet@efsora.com", "pass123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Login should fail when password is wrong")
    void login_sifreYanlis() {
        when(kullaniciRepository.findByEmail("ahmet@efsora.com")).thenReturn(Optional.of(testKullanici));
        when(passwordEncoder.matches("wrong", testKullanici.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> kullaniciService.login("ahmet@efsora.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Password change should fail when new password equals current password")
    void sifreDegistir_ayniSifreReddedilir() {
        when(kullaniciRepository.findByEmail("ahmet@efsora.com")).thenReturn(Optional.of(testKullanici));
        when(passwordEncoder.matches("same-pass", testKullanici.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> kullaniciService.sifreDegistir("ahmet@efsora.com", "same-pass", "same-pass"))
                .isInstanceOf(SamePasswordException.class)
                .hasMessageContaining("different from current password");

        verify(kullaniciRepository, never()).save(any(Kullanici.class));
    }
}
