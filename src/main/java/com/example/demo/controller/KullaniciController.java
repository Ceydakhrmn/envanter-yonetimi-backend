package com.example.demo.controller;

import com.example.demo.dto.KullaniciMapper;
import com.example.demo.dto.KullaniciRequestDTO;
import com.example.demo.dto.KullaniciResponseDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping
    public ResponseEntity<KullaniciResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New user data",
                required = true
            ) @Valid @RequestBody KullaniciRequestDTO requestDTO) {
        log.info("API: Creating new user");
        Kullanici entity = mapper.toEntity(requestDTO);
        Kullanici created = service.kullaniciOlustur(entity);
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
        return ResponseEntity.ok(mapper.toResponseDTO(updated));
    }

    @Operation(summary = "Delete user (soft delete)", description = "Marks user as inactive without removing from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("API: Deleting user with ID={}", id);
        service.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check", description = "Checks if the User Management API is running")
    @ApiResponse(responseCode = "200", description = "API is operational")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User API is running! ✅");
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
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> kullaniciKaliciSil(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.warn("API: Permanently deleting user ID={}", id);
        service.kullaniciKaliciSil(id);
        return ResponseEntity.noContent().build();
    }
}
