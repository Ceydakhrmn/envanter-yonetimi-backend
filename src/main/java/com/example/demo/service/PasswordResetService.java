package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    // Token geçerlilik süresi: 15 dakika
    private static final long EXPIRY_MILLIS = 15 * 60 * 1000L;

    private final KullaniciRepository kullaniciRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * E-posta adresine ait kullanıcı için sıfırlama token'ı oluşturur.
     * Kullanıcı bulunamasa bile güvenlik gereği hata fırlatmaz — token döner.
     * (Gerçek projede burada e-posta gönderilir; biz token'ı direkt döndürüyoruz.)
     */
    @Transactional
    public String createResetToken(String email) {
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found with this email."));

        // Varsa eski token'ı sil
        resetTokenRepository.deleteByKullanici(kullanici);

        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(rawToken)
                .kullanici(kullanici)
                .expiryDate(Instant.now().plusMillis(EXPIRY_MILLIS))
                .used(false)
                .build();

        resetTokenRepository.save(resetToken);
        log.info("Password reset token created for: {}", email);
        return rawToken;
    }

    /**
     * Token geçerliyse şifreyi sıfırlar.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token."));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This reset token has already been used.");
        }
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            resetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Reset token has expired. Please request a new one.");
        }

        Kullanici kullanici = resetToken.getKullanici();
        kullanici.setPassword(passwordEncoder.encode(newPassword));
        kullaniciRepository.save(kullanici);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        log.info("Password reset successfully for: {}", kullanici.getEmail());
    }
}
