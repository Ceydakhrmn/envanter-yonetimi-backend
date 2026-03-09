package com.example.demo.controller;

import com.example.demo.dto.MessageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base Controller - Generic CRUD operations
 * All entity controllers can extend this to get standard CRUD endpoints
 * 
 * @param <T> Entity type
 * @param <ID> ID type (usually Long)
 * @param <S> Service type
 */
@Slf4j
public abstract class BaseController<T, ID, S extends BaseController.BaseService<T, ID>> {

    protected final S service;
    protected final String entityName;

    protected BaseController(S service, String entityName) {
        this.service = service;
        this.entityName = entityName;
    }

    /**
     * Interface that all services must implement for base CRUD operations
     */
    public interface BaseService<T, ID> {
        List<T> findAll();
        T findById(ID id);
        T create(T entity);
        T update(ID id, T entity);
        void delete(ID id);
    }

    /**
     * List all entities
     */
    @Operation(summary = "List all entities")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<List<T>> findAll() {
        log.info("API: Listing all {}", entityName);
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Find entity by ID
     */
    @Operation(summary = "Find entity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entity found"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<T> findById(
            @Parameter(description = "Entity ID") @PathVariable ID id) {
        log.info("API: Searching for {} with ID={}", entityName, id);
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Create new entity
     */
    @Operation(summary = "Create new entity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entity created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping
    public ResponseEntity<T> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New entity data",
                required = true
            ) @Valid @RequestBody T entity) {
        log.info("API: Creating new {}", entityName);
        T created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update entity
     */
    @Operation(summary = "Update entity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entity updated"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<T> update(
            @Parameter(description = "Entity ID") @PathVariable ID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated entity data",
                required = true
            ) @Valid @RequestBody T entity) {
        log.info("API: Updating {} with ID={}", entityName, id);
        return ResponseEntity.ok(service.update(id, entity));
    }

    /**
     * Delete entity (soft delete)
     */
    @Operation(summary = "Delete entity (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Entity deleted"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity ID") @PathVariable ID id) {
        log.info("API: Deleting {} with ID={}", entityName, id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check endpoint
     */
    @Operation(summary = "Health check")
    @ApiResponse(
        responseCode = "200",
        description = "API is running",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = MessageResponseDTO.class)
        )
    )
    @GetMapping("/health")
    public ResponseEntity<MessageResponseDTO> health() {
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message(entityName + " API is running! ✅")
                .build());
    }
}
