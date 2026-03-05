package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error Response DTO
 * Used for returning error messages in API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response with message and timestamp")
public class ErrorResponseDTO {

    @Schema(description = "Error message", example = "Email already registered")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Timestamp when error occurred", example = "2026-03-05T12:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Field-level validation errors")
    private Map<String, String> errors;
}
