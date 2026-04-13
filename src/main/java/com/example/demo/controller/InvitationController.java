package com.example.demo.controller;

import com.example.demo.entity.Invitation;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.InvitationService;
import com.example.demo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
public class InvitationController {

    private final InvitationService invitationService;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createInvitation(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String role = body.getOrDefault("role", "USER");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (kullaniciRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already exists"));
        }

        Invitation invitation = invitationService.createInvitation(email, role);
        log.info("Invitation created for: {}", email);

        return ResponseEntity.ok(Map.of(
            "token", invitation.getToken(),
            "email", invitation.getEmail(),
            "expiresAt", invitation.getExpiresAt().toString(),
            "inviteLink", "/invite/" + invitation.getToken()
        ));
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyToken(@PathVariable String token) {
        try {
            Invitation invitation = invitationService.findByToken(token);
            if (!invitationService.isValid(invitation)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invitation expired or already used"));
            }
            return ResponseEntity.ok(Map.of(
                "email", invitation.getEmail(),
                "role", invitation.getRole(),
                "valid", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid invitation token"));
        }
    }

    @PostMapping("/accept/{token}")
    public ResponseEntity<?> acceptInvitation(@PathVariable String token, @RequestBody Map<String, String> body) {
        try {
            Invitation invitation = invitationService.findByToken(token);
            if (!invitationService.isValid(invitation)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invitation expired or already used"));
            }

            String email = invitation.getEmail();

            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String password = body.get("password");

            if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()
                    || password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "firstName, lastName and password (min 6 chars) are required"));
            }

            Kullanici kullanici = new Kullanici();
            kullanici.setFirstName(firstName.trim());
            kullanici.setLastName(lastName.trim());
            kullanici.setEmail(email);
            kullanici.setPassword(passwordEncoder.encode(password));
            kullanici.setDepartment("");
            kullanici.setRegistrationDate(LocalDateTime.now());
            kullanici.setActive(true);
            kullanici.setRole(Kullanici.Role.valueOf(invitation.getRole()));

            Kullanici saved = kullaniciRepository.save(kullanici);
            invitationService.markAsUsed(invitation);

            String jwtToken = jwtUtil.generateToken(saved);
            var refreshToken = refreshTokenService.createRefreshToken(saved.getId());

            return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "refreshToken", refreshToken.getToken(),
                "email", saved.getEmail(),
                "firstName", saved.getFirstName(),
                "lastName", saved.getLastName()
            ));
        } catch (Exception e) {
            log.error("Error accepting invitation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
