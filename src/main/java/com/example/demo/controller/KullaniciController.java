package com.example.demo.controller;

import com.example.demo.dto.KullaniciMapper;
import com.example.demo.dto.KullaniciRequestDTO;
import com.example.demo.dto.KullaniciResponseDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.ChangePasswordRequestDTO;
import com.example.demo.dto.MessageResponseDTO;
import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
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

    // ==================== CRUD Operations ====================

    @Operation(summary = "List all users", description = "Returns all users in the system")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<List<KullaniciResponseDTO>> findAll() {
        log.info("API: Listing all users");
        List<Kullanici> entities = service.tumKullanicilar();
        return ResponseEntity.ok(mapper.toResponseDTOList(entities));
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
}
