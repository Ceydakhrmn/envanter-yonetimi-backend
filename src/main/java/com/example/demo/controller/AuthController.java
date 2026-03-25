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

    /**
     * User Registration
     * Creates a new user account
     */
    @Operation(summary = "Register new user", description = "Creates a new user account and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input or email already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody KullaniciRequestDTO request) {
        log.info("[REGISTER] Yeni kayıt isteği alındı. Request body: {}", request);

        // Check if email already exists
        if (kullaniciRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Create new user
        Kullanici kullanici = new Kullanici();
        kullanici.setFirstName(request.getFirstName());
        kullanici.setLastName(request.getLastName());
        kullanici.setEmail(request.getEmail());
        kullanici.setPassword(passwordEncoder.encode(request.getPassword()));
        kullanici.setDepartment(request.getDepartment());
        kullanici.setRegistrationDate(LocalDateTime.now());
        kullanici.setActive(true);

        // Save to database
        Kullanici savedUser = kullaniciRepository.save(kullanici);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);
        
        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        // Build response
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .refreshToken(refreshToken.getToken())
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .department(savedUser.getDepartment())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());

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

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);
            
        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(kullanici.getId());

        log.info("Login successful for user: {}", kullanici.getEmail());

        // Build response
        AuthResponseDTO response = AuthResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .refreshToken(refreshToken.getToken())
            .id(kullanici.getId())
            .email(kullanici.getEmail())
            .firstName(kullanici.getFirstName())
            .lastName(kullanici.getLastName())
            .department(kullanici.getDepartment())
            .build();

        return ResponseEntity.ok(response);
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
