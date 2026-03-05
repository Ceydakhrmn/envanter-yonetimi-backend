package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token Request DTO
 * Used to request a new access token using refresh token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request")
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token boş olamaz")
    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
}
