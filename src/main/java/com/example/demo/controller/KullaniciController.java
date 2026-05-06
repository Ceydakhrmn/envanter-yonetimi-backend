import com.example.demo.dto.ForgotPasswordRequestDTO;
package com.example.demo.controller;

import com.example.demo.dto.KullaniciMapper;
import com.example.demo.dto.KullaniciRequestDTO;
import com.example.demo.dto.KullaniciResponseDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.ChangePasswordRequestDTO;
import com.example.demo.dto.MessageResponseDTO;
import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import com.example.demo.service.MFAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.example.demo.dto.PagedResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * User Controller - REST API endpoints with DTO pattern
 * Uses DTOs (Data Transfer Objects) instead of directly exposing entities
 * This provides better security and API contract management
 */
@RestController
@RequestMapping("/api/kullanicilar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API endpoints for user CRUD operations")
public class KullaniciController {

    private final KullaniciService service;
    private final KullaniciMapper mapper;
    private final com.example.demo.service.ActivityLogService activityLogService;
    private final com.example.demo.security.JwtUtil jwtUtil;
    private final com.example.demo.service.NotificationService notificationService;
    @Autowired
    private MFAService mfaService;

    // ==================== CRUD Operations ====================

    @Operation(summary = "List all users", description = "Returns all users in the system with pagination and advanced filtering support")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
        @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email if exists")
        @PostMapping("/forgot-password")
        public ResponseEntity<MessageResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
            log.info("API: Forgot password request for email={}", request.getEmail());
            service.forgotPassword(request.getEmail());
            return ResponseEntity.ok(MessageResponseDTO.builder()
                    .message("If this email exists, a reset link has been sent.")
                    .build());
        }
    public ResponseEntity<PagedResponseDTO<KullaniciResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        log.info("API: Listing all users - page: {}, size: {}, department: {}, role: {}, active: {}, search: {}", page, size, department, role, active, search);
        List<Kullanici> allUsers = service.tumKullanicilar();
        // Filtreleme
        if (department != null && !department.equals("all")) {
            allUsers = allUsers.stream().filter(u -> department.equals(u.getDepartment())).toList();
        }
        if (role != null && !role.equals("all")) {
            allUsers = allUsers.stream().filter(u -> u.getRole() != null && role.equalsIgnoreCase(u.getRole().name())).toList();
        }
        if (active != null) {
            allUsers = allUsers.stream().filter(u -> active.equals(u.getActive())).toList();
        }
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            allUsers = allUsers.stream().filter(u ->
                (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(s)) ||
                (u.getLastName() != null && u.getLastName().toLowerCase().contains(s)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(s))
            ).toList();
        }
        // Simple in-memory pagination
        int totalElements = allUsers.size();
        int totalPages = (totalElements + size - 1) / size;
        // Validate page
        if (page < 0) page = 0;
        if (page >= totalPages && totalElements > 0) page = totalPages - 1;
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<KullaniciResponseDTO> pageContent = mapper.toResponseDTOList(
            allUsers.subList(start, end)
        );
        PagedResponseDTO<KullaniciResponseDTO> response = PagedResponseDTO.<KullaniciResponseDTO>builder()
            .content(pageContent)
            .currentPage(page)
            .pageSize(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(page < totalPages - 1)
            .hasPrevious(page > 0)
            .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Find user by ID", description = "Returns a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<KullaniciResponseDTO> findById(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("API: Searching for user with ID={}", id);
        Kullanici entity = service.kullaniciBul(id);
        return ResponseEntity.ok(mapper.toResponseDTO(entity));
    }

    @Operation(summary = "Create new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<KullaniciResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New user data",
                required = true
            ) @Valid @RequestBody KullaniciRequestDTO requestDTO) {
        log.info("API: Creating new user");
        Kullanici entity = mapper.toEntity(requestDTO);
        Kullanici created = service.kullaniciOlustur(entity);
        activityLogService.log("CREATE", "USER", created.getId(), "Kullanıcı oluşturuldu: " + created.getEmail());
        notificationService.create("success", "Hesabınız oluşturuldu. Hoş geldiniz!", created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(created));
    }

    @Operation(summary = "Update user", description = "Updates an existing user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<KullaniciResponseDTO> update(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user data",
                required = true
            ) @Valid @RequestBody KullaniciRequestDTO requestDTO) {
        log.info("API: Updating user with ID={}", id);
        Kullanici existingEntity = service.kullaniciBul(id);
        mapper.updateEntityFromDTO(requestDTO, existingEntity);
        Kullanici updated = service.kullaniciGuncelle(id, existingEntity);
        activityLogService.log("UPDATE", "USER", id, "Kullanıcı güncellendi: " + updated.getEmail());
        notificationService.create("info", "Profil bilgileriniz güncellendi.", updated.getEmail());
        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @Operation(summary = "Delete user (soft delete)", description = "Marks user as inactive without removing from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("API: Deleting user with ID={}", id);
        service.kullaniciSil(id);
        activityLogService.log("DELETE", "USER", id, "Kullanıcı silindi (soft)");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk delete users", description = "Deletes multiple users by their IDs (hard delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Users deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<Long> ids) {
        log.info("API: Bulk deleting users: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        service.kullanicilariTopluSil(ids);
        activityLogService.log("BULK_DELETE", "USER", null, "Toplu silme: " + ids.size() + " kullanıcı");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check", description = "Checks if the User Management API is running")
    @ApiResponse(
        responseCode = "200",
        description = "API is operational",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageResponseDTO.class)
        )
    )
    @GetMapping("/health")
    public ResponseEntity<MessageResponseDTO> health() {
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message("User API is running! ✅")
                .build());
    }

    // ==================== User-Specific Endpoints ====================

    @Operation(summary = "List active users", description = "Lists only active (not deleted) users")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/active")
    public ResponseEntity<List<KullaniciResponseDTO>> activeUsers() {
        log.info("API: Listing active users");
        List<Kullanici> entities = service.activeUsers();
        return ResponseEntity.ok(mapper.toResponseDTOList(entities));
    }

    @Operation(summary = "Find user by email", description = "Searches for user by email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<KullaniciResponseDTO> emailIleKullaniciBul(
            @Parameter(description = "Email address", example = "ahmet@efsora.com") @PathVariable String email) {
        log.info("API: Searching for user by email: {}", email);
        Kullanici entity = service.emailIleKullaniciBul(email);
        return ResponseEntity.ok(mapper.toResponseDTO(entity));
    }

    @Operation(summary = "List by department", description = "Lists users in a specific department")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/departman/{departman}")
    public ResponseEntity<List<KullaniciResponseDTO>> departmanaGoreListele(
            @Parameter(description = "Department name", example = "IT") @PathVariable String departman) {
        log.info("API: Listing by department: {}", departman);
        List<Kullanici> entities = service.departmanaGoreListele(departman);
        return ResponseEntity.ok(mapper.toResponseDTOList(entities));
    }

    // ==================== Authentication ====================

    @Operation(summary = "User login", description = "Validates user credentials (email and password)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<KullaniciResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("API: Login attempt for email: {}", loginRequest.getEmail());
        Kullanici kullanici = service.login(loginRequest.getEmail(), loginRequest.getPassword());
        log.info("Login successful for: {}", loginRequest.getEmail());
        return ResponseEntity.ok(mapper.toResponseDTO(kullanici));
    }

    @Operation(summary = "Permanently delete user", description = "Completely removes user from database (irreversible!)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User permanently deleted"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> kullaniciKaliciSil(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.warn("API: Permanently deleting user ID={}", id);
        service.kullaniciKaliciSil(id);
        activityLogService.log("PERMANENT_DELETE", "USER", id, "Kullanıcı kalıcı silindi");
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change password", description = "Allows authenticated user to set a new password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid current/new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponseDTO> sifreDegistir(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        log.info("API: Password change request for email={}", userDetails.getUsername());
        service.sifreDegistir(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message("Password changed successfully")
                .build());
    }

    @Operation(summary = "Upload profile photo", description = "Uploads base64 encoded profile photo")
    @PostMapping("/{id}/photo")
    public ResponseEntity<KullaniciResponseDTO> uploadPhoto(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        log.info("API: Uploading photo for user ID={}", id);
        String base64Photo = body.get("photo");
        Kullanici kullanici = service.kullaniciBul(id);
        kullanici.setProfilePhoto(base64Photo);
        Kullanici updated = service.kullaniciGuncelle(id, kullanici);
        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @Operation(summary = "Impersonate user", description = "Admin generates a temporary JWT for another user to debug issues")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Impersonation token generated"),
        @ApiResponse(responseCode = "403", description = "Only admins can impersonate"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/impersonate")
    public ResponseEntity<com.example.demo.dto.AuthResponseDTO> impersonate(
            @Parameter(description = "Target user ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails adminUser) {
        log.warn("API: Admin {} impersonating user ID={}", adminUser.getUsername(), id);
        Kullanici target = service.kullaniciBul(id);

        // Impersonation claim ekle
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("impersonatedBy", adminUser.getUsername());
        claims.put("isImpersonation", true);
        String token = jwtUtil.generateToken(target, claims);

        activityLogService.log("IMPERSONATE", "USER", id, "Admin " + adminUser.getUsername() + " kullanıcı " + target.getEmail() + " olarak giriş yaptı");

        com.example.demo.dto.AuthResponseDTO response = com.example.demo.dto.AuthResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .id(target.getId())
            .email(target.getEmail())
            .firstName(target.getFirstName())
            .lastName(target.getLastName())
            .department(target.getDepartment())
            .role(target.getRole() != null ? target.getRole().name() : "USER")
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bulk import users", description = "Creates multiple users from a list (CSV import)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/bulk-import")
    public ResponseEntity<java.util.Map<String, Object>> bulkImport(@RequestBody List<KullaniciRequestDTO> users) {
        log.info("API: Bulk importing {} users", users.size());
        if (users == null || users.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<com.example.demo.entity.Kullanici> entities = users.stream().map(dto -> {
            com.example.demo.entity.Kullanici k = mapper.toEntity(dto);
            return k;
        }).toList();

        List<java.util.Map<String, Object>> results = service.topluKullaniciEkle(entities);
        long success = results.stream().filter(r -> "success".equals(r.get("status"))).count();
        long failed = results.stream().filter(r -> "error".equals(r.get("status"))).count();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("total", users.size());
        response.put("success", success);
        response.put("failed", failed);
        response.put("results", results);

        activityLogService.log("BULK_IMPORT", "USER", null, "Toplu içe aktarma: " + success + " başarılı, " + failed + " başarısız");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable MFA/2FA", description = "Enables MFA for the authenticated user (TOTP)")
    @PostMapping("/mfa/enable")
    public ResponseEntity<?> enableMfa(@AuthenticationPrincipal UserDetails userDetails) {
        Kullanici user = service.emailIleKullaniciBul(userDetails.getUsername());
        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        user.setMfaType(Kullanici.MfaType.TOTP);
        List<String> backupCodes = mfaService.generateBackupCodes();
        user.setMfaBackupCodes(mfaService.hashBackupCodes(backupCodes));
        service.kullaniciGuncelle(user.getId(), user);
        return ResponseEntity.ok(java.util.Map.of(
            "secret", secret,
            "backupCodes", backupCodes
        ));
    }

    @Operation(summary = "Disable MFA/2FA", description = "Disables MFA for the authenticated user")
    @PostMapping("/mfa/disable")
    public ResponseEntity<?> disableMfa(@AuthenticationPrincipal UserDetails userDetails) {
        Kullanici user = service.emailIleKullaniciBul(userDetails.getUsername());
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaType(Kullanici.MfaType.NONE);
        user.setMfaBackupCodes(null);
        service.kullaniciGuncelle(user.getId(), user);
        return ResponseEntity.ok(java.util.Map.of("message", "MFA disabled"));
    }

    @Operation(summary = "Verify MFA/2FA code", description = "Verifies a TOTP or backup code for the authenticated user")
    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@AuthenticationPrincipal UserDetails userDetails, @RequestBody java.util.Map<String, String> body) {
        Kullanici user = service.emailIleKullaniciBul(userDetails.getUsername());
        String code = body.get("code");
        boolean valid = false;
        if (user.getMfaType() == Kullanici.MfaType.TOTP && user.getMfaSecret() != null) {
            valid = mfaService.validateCode(user.getMfaSecret(), code);
        } else if (user.getMfaType() == Kullanici.MfaType.BACKUP && user.getMfaBackupCodes() != null) {
            valid = mfaService.validateBackupCode(user, code);
            if (valid) {
                user.setMfaBackupCodes(mfaService.removeBackupCode(user.getMfaBackupCodes(), code));
                service.kullaniciGuncelle(user.getId(), user);
            }
        }
        if (valid) {
            return ResponseEntity.ok(java.util.Map.of("valid", true));
        } else {
            return ResponseEntity.status(401).body(java.util.Map.of("valid", false, "message", "Invalid MFA code"));
        }
    }

    @Operation(summary = "Get MFA backup codes", description = "Returns current backup codes for the authenticated user (for demo, not production)")
    @GetMapping("/mfa/backup-codes")
    public ResponseEntity<?> getBackupCodes(@AuthenticationPrincipal UserDetails userDetails) {
        Kullanici user = service.emailIleKullaniciBul(userDetails.getUsername());
        if (user.getMfaBackupCodes() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "No backup codes set"));
        }
        return ResponseEntity.ok(java.util.Map.of(
            "backupCodes", user.getMfaBackupCodes().split(",")
        ));
    }
}
