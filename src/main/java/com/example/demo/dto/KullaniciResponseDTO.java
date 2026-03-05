package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * User Response DTO - Used for returning user data in API responses
 * Contains all user information except password
 */
@Data
@Schema(description = "User response data")
public class KullaniciResponseDTO {
    
    @Schema(description = "User's unique identifier", example = "1")
    private Long id;

    @Schema(description = "User's first name", example = "Ceyda")
    private String firstName;

    @Schema(description = "User's last name", example = "Efsora")
    private String lastName;

    @Schema(description = "User's email address", example = "ceyda@efsora.com")
    private String email;

    @Schema(description = "User's department", example = "IT")
    private String department;

    @Schema(description = "Date and time when user registered", example = "2026-03-05T12:00:00")
    private java.time.LocalDateTime registrationDate;

    @Schema(description = "Indicates if user account is active", example = "true")
    private Boolean active;
}
