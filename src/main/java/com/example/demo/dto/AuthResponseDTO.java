package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 * Contains JWT token and user information after successful login/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token")
public class AuthResponseDTO {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String type = "Bearer";

    @Schema(description = "Refresh token for getting new access token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's email", example = "ceyda@efsora.com")
    private String email;

    @Schema(description = "User's first name", example = "Ceyda")
    private String firstName;

    @Schema(description = "User's last name", example = "Efsora")
    private String lastName;

    @Schema(description = "User's department", example = "IT")
    private String department;
}
