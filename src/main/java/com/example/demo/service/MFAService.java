package com.example.demo.service;

import com.example.demo.entity.Kullanici;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Service
public class MFAService {

    private static final int TIME_STEP = 30;
    private static final int DIGITS = 6;
    private static final int WINDOW = 1;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return new Base32().encodeToString(bytes).replace("=", "");
    }

    public boolean validateCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            long timeStep = System.currentTimeMillis() / 1000 / TIME_STEP;
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(secret.toUpperCase());
            for (int i = -WINDOW; i <= WINDOW; i++) {
                if (generateTOTP(secretBytes, timeStep + i) == codeInt) {
                    return true;
                }
            }
            return false;
        } catch (NumberFormatException | InvalidKeyException | NoSuchAlgorithmException e) {
            return false;
        }
    }

    private int generateTOTP(byte[] secret, long timeStep) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        return binary % (int) Math.pow(10, DIGITS);
    }

    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            codes.add(UUID.randomUUID().toString().substring(0, 8));
        }
        return codes;
    }

    public String hashBackupCodes(List<String> codes) {
        return String.join(",", codes);
    }

    public boolean validateBackupCode(Kullanici user, String code) {
        if (user.getMfaBackupCodes() == null) return false;
        List<String> codes = Arrays.asList(user.getMfaBackupCodes().split(","));
        return codes.contains(code);
    }

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
