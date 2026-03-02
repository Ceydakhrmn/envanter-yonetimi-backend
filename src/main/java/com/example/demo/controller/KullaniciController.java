package com.example.demo.controller;

import com.example.demo.entity.Kullanici;
import com.example.demo.service.KullaniciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    // ==================== Override BaseController Methods with User-Specific Descriptions ====================

    @Override
    @Operation(summary = "List all users", description = "Returns all users in the system")
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<List<Kullanici>> findAll() {
        return super.findAll();
    }

    @Override
    @Operation(summary = "Find user by ID", description = "Returns a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Kullanici> findById(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        return super.findById(id);
    }

    @Override
    @Operation(summary = "Create new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Kullanici> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New user data",
                required = true
            ) @Valid @RequestBody Kullanici entity) {
        return super.create(entity);
    }

    @Override
    @Operation(summary = "Update user", description = "Updates an existing user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Kullanici> update(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated user data",
                required = true
            ) @Valid @RequestBody Kullanici entity) {
        return super.update(id, entity);
    }

    @Override
    @Operation(summary = "Delete user (soft delete)", description = "Marks user as inactive without removing from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @Operation(summary = "Health check", description = "Checks if the User Management API is running")
    @ApiResponse(responseCode = "200", description = "API is operational")
    public ResponseEntity<String> health() {
        return super.health();
    }

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
