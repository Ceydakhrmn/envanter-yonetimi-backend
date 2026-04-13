package com.example.demo.controller;

import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.ErrorResponseDTO;
import com.example.demo.dto.ForgotPasswordRequestDTO;
import com.example.demo.dto.KullaniciRequestDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.MessageResponseDTO;
import com.example.demo.dto.RefreshTokenRequestDTO;
import com.example.demo.dto.ResetPasswordRequestDTO;
import com.example.demo.entity.Kullanici;
import com.example.demo.entity.RefreshToken;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.RefreshTokenNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Authentication Controller
 * Handles user registration and login with JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final com.example.demo.service.ActivityLogService activityLogService;

    /**
     * Public registration is disabled.
     * Users can only join via admin invitation.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Public registration is disabled. Please use an invitation link."));
    }

    /**
     * User Login
     * Authenticates user and returns JWT token
     */
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login successful",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid credentials",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Account lockout kontrolü
        var optionalUser = kullaniciRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            Kullanici k = optionalUser.get();
            if (k.getLockExpiresAt() != null && k.getLockExpiresAt().isAfter(LocalDateTime.now())) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), k.getLockExpiresAt()).toMinutes() + 1;
                activityLogService.log("LOGIN_BLOCKED", "AUTH", k.getId(), "Hesap kilitli. " + minutesLeft + " dk kaldı. Email: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(java.util.Map.of("message", "Account is locked. Try again in " + minutesLeft + " minutes.", "lockedUntil", k.getLockExpiresAt().toString()));
            }
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Get authenticated user
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Başarılı giriş: lockout alanlarını sıfırla
            kullanici.setFailedLoginAttempts(0);
            kullanici.setLockExpiresAt(null);
            kullanici.setLastLoginDate(LocalDateTime.now());
            kullaniciRepository.save(kullanici);

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(kullanici.getId());

            log.info("Login successful for user: {}", kullanici.getEmail());
            activityLogService.log("LOGIN", "AUTH", kullanici.getId(), "Başarılı giriş: " + kullanici.getEmail());

            // Şifre süresi kontrolü (90 gün)
            boolean passwordExpired = false;
            if (kullanici.getPasswordChangedAt() != null) {
                passwordExpired = kullanici.getPasswordChangedAt().plusDays(90).isBefore(LocalDateTime.now());
            }

            AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .id(kullanici.getId())
                .email(kullanici.getEmail())
                .firstName(kullanici.getFirstName())
                .lastName(kullanici.getLastName())
                .department(kullanici.getDepartment())
                .role(kullanici.getRole() != null ? kullanici.getRole().name() : "USER")
                .lastLoginDate(kullanici.getLastLoginDate())
                .passwordExpired(passwordExpired)
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Başarısız giriş: sayacı artır
            optionalUser.ifPresent(k -> {
                int attempts = (k.getFailedLoginAttempts() == null ? 0 : k.getFailedLoginAttempts()) + 1;
                k.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    k.setLockExpiresAt(LocalDateTime.now().plusMinutes(15));
                    log.warn("Account locked for 15 minutes: {}", request.getEmail());
                    activityLogService.log("ACCOUNT_LOCKED", "AUTH", k.getId(), "5 başarısız giriş - hesap 15 dk kilitlendi. Email: " + request.getEmail());
                } else {
                    activityLogService.log("LOGIN_FAILED", "AUTH", k.getId(), "Başarısız giriş denemesi (" + attempts + "/5). Email: " + request.getEmail());
                }
                kullaniciRepository.save(k);
            });
            throw e;
        }
    }

    /**
     * Test endpoint to verify JWT authentication
     */
    @Operation(summary = "Test authentication", description = "Verifies that JWT token is working")
    @ApiResponse(
        responseCode = "200",
        description = "Token is valid",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageResponseDTO.class)
        )
    )
    @GetMapping("/test")
    public ResponseEntity<MessageResponseDTO> test() {
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message("JWT Authentication is working! OK")
                .build());
    }

    /**
     * Refresh Token
     * Returns a new access token using refresh token
     */
    @Operation(summary = "Refresh access token", description = "Returns a new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid or expired refresh token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("Refresh token request received");

        // Find and verify refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token bulunamadı"));

        // Verify expiration
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        // Get user
        Kullanici kullanici = refreshToken.getKullanici();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(kullanici);

        log.info("Access token refreshed for user: {}", kullanici.getEmail());

        // Build response
        AuthResponseDTO response = AuthResponseDTO.builder()
            .token(newAccessToken)
            .type("Bearer")
            .refreshToken(refreshToken.getToken())
            .id(kullanici.getId())
            .email(kullanici.getEmail())
            .firstName(kullanici.getFirstName())
            .lastName(kullanici.getLastName())
            .department(kullanici.getDepartment())
            .role(kullanici.getRole() != null ? kullanici.getRole().name() : "USER")
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Logout
     * Invalidates user's refresh token
     */
    @Operation(summary = "User logout", description = "Invalidates user's refresh token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Logout successful"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Invalid refresh token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(@RequestBody RefreshTokenRequestDTO request) {
        log.info("Logout request received");

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token bulunamadı"));

        refreshTokenService.deleteByKullaniciId(refreshToken.getKullanici().getId());

        log.info("User logged out successfully");
        return ResponseEntity.ok(MessageResponseDTO.builder()
            .message("Logout başarılı")
            .build());
    }

    /**
     * Forgot Password — reset token oluşturur
     */
    @Operation(summary = "Forgot password", description = "Creates a password reset token for the given email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reset token returned"),
        @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("Forgot password request for: {}", request.getEmail());
        String token = passwordResetService.createResetToken(request.getEmail());
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message(token)
                .build());
    }

    /**
     * Reset Password — token + yeni şifre ile şifreyi sıfırlar
     */
    @Operation(summary = "Reset password", description = "Resets password using a valid reset token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        log.info("Reset password request received");
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message("Password reset successfully.")
                .build());
    }
}
