package com.example.demo.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller - REST API endpoints
 * Frontend retrieves and sends data through here
 */
@RestController
@RequestMapping("Kullanicilar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "API endpoints for user CRUD operations")
public class KullaniciController {

    private final KullaniciService kullaniciService;

    @Operation(summary = "List all users", description = "Lists all users (active and inactive) in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Kullanici.class)))
    })
    @GetMapping
    public ResponseEntity<List<Kullanici>> tumKullanicilar() {
        log.info("API: Listing all users");
        return ResponseEntity.ok(kullaniciService.tumKullanicilar());
    }

    @Operation(summary = "List active users", description = "Lists only active (not deleted) users")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("aktif")
    public ResponseEntity<List<Kullanici>> aktifKullanicilar() {
        log.info("API: Listing active users");
        return ResponseEntity.ok(kullaniciService.aktifKullanicilar());
    }

    @Operation(summary = "Find user by ID", description = "Retrieves details of a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("{id}")
    public ResponseEntity<Kullanici> kullaniciBul(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("API: Searching for user ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciBul(id));
    }

    @Operation(summary = "Find user by email", description = "Searches for user by email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("email")
    public ResponseEntity<Kullanici> emailIleKullaniciBul(
            @Parameter(description = "Email address", example = "ahmet@efsora.com") @PathVariable String email) {
        log.info("API: Searching for user by email: {}", email);
        return ResponseEntity.ok(kullaniciService.emailIleKullaniciBul(email));
    }

    @Operation(summary = "List by department", description = "Lists users in a specific department")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("departman")
    public ResponseEntity<List<Kullanici>> departmanaGoreListele(
            @Parameter(description = "Department name", example = "IT") @PathVariable String departman) {
        log.info("API: Listing by department: {}", departman);
        return ResponseEntity.ok(kullaniciService.departmanaGoreListele(departman));
    }

    @Operation(summary = "Create new user", description = "Adds a new user to the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data or email already in use")
    })
    @PostMapping
    public ResponseEntity<Kullanici> kullaniciOlustur(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New user information",
                required = true
            ) @Valid @RequestBody Kullanici kullanici) {
        log.info("API: Creating new user: {}", kullanici.getEmail());
        Kullanici yeniKullanici = kullaniciService.kullaniciOlustur(kullanici);
        return ResponseEntity.status(HttpStatus.CREATED).body(yeniKullanici);
    }

    @Operation(summary = "Update user", description = "Updates existing user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciGuncelle(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user information",
                required = true
            ) @Valid @RequestBody Kullanici kullanici) {
        log.info("API: Updating user ID={}", id);
        return ResponseEntity.ok(kullaniciService.kullaniciGuncelle(id, kullanici));
    }

    @Operation(summary = "Delete user (soft delete)", description = "Deactivates user, data is preserved")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kullaniciSil(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("API: Deleting user (deactivating) ID={}", id);
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Permanently delete user", description = "Completely removes user from database (irreversible!)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User permanently deleted"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}/kalici")
    public ResponseEntity<Void> kullaniciKaliciSil(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.warn("API: Permanently deleting user ID={}", id);
        kullaniciService.kullaniciKaliciSil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Health check", description = "Checks if the API is running")
    @ApiResponse(responseCode = "200", description = "API is running")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User API is running! ✅");
    }
}
