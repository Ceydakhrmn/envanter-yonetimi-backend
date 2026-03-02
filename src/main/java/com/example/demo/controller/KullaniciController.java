package com.example.demo.controller;

import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller - REST API endpoints
 * Extends BaseController for standard CRUD operations
 * Contains only user-specific endpoints (email search, department filter, etc.)
 */
@RestController
@RequestMapping("/api/kullanicilar")
@Slf4j
@Tag(name = "User Management", description = "API endpoints for user CRUD operations")
public class KullaniciController extends BaseController<Kullanici, Long, KullaniciService> {

    public KullaniciController(KullaniciService service) {
        super(service, "User");
    }

    // ==================== User-Specific Endpoints (Not in BaseController) ====================

    // ==================== User-Specific Endpoints (Not in BaseController) ====================

    @Operation(summary = "List active users", description = "Lists only active (not deleted) users")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/aktif")
    public ResponseEntity<List<Kullanici>> aktifKullanicilar() {
        log.info("API: Listing active users");
        return ResponseEntity.ok(service.aktifKullanicilar());
    }

    @Operation(summary = "Find user by email", description = "Searches for user by email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Kullanici> emailIleKullaniciBul(
            @Parameter(description = "Email address", example = "ahmet@efsora.com") @PathVariable String email) {
        log.info("API: Searching for user by email: {}", email);
        return ResponseEntity.ok(service.emailIleKullaniciBul(email));
    }

    @Operation(summary = "List by department", description = "Lists users in a specific department")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/departman/{departman}")
    public ResponseEntity<List<Kullanici>> departmanaGoreListele(
            @Parameter(description = "Department name", example = "IT") @PathVariable String departman) {
        log.info("API: Listing by department: {}", departman);
        return ResponseEntity.ok(service.departmanaGoreListele(departman));
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
