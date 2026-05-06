package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service

public class MFAService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // Generate a new TOTP secret (Base32)
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    // Validate TOTP code
    public boolean validateCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Generate backup codes (CSV, hashed)
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            codes.add(UUID.randomUUID().toString().substring(0, 8));
        }
        return codes;
    }

    // Hash backup codes (for storage)
    public String hashBackupCodes(List<String> codes) {
        // For demo: store as CSV (in production, hash with bcrypt)
        return String.join(",", codes);
    }

    // Validate backup code
    public boolean validateBackupCode(Kullanici user, String code) {
        if (user.getMfaBackupCodes() == null) return false;
        List<String> codes = Arrays.asList(user.getMfaBackupCodes().split(","));
        return codes.contains(code);
    }

    // Remove used backup code
    public String removeBackupCode(String backupCodes, String usedCode) {
        List<String> codes = new ArrayList<>(Arrays.asList(backupCodes.split(",")));
        codes.remove(usedCode);
        return String.join(",", codes);
    }

    public boolean verifyCode(String secret, String code) {
        return validateCode(secret, code);
    }

    public String generateQrCodeUrl(String secret, String email) {
        return "otpauth://totp/EfsoraBackend:" + email + "?secret=" + secret + "&issuer=EfsoraBackend";
    }
}
