package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Request DTO - Used for creating and updating users
 * Does not contain technical fields (id, registration date, active status)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User request data for create/update operations")
public class KullaniciRequestDTO {

    @Schema(description = "User's first name", example = "Ahmet", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName; // Changed to camelCase

    @Schema(description = "User's last name", example = "Yılmaz", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName; // Changed to camelCase

    @Schema(description = "User's email address (must be unique)", example = "ahmet@efsora.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email; // Changed to camelCase

    @Schema(description = "User's department", example = "IT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Department is required")
    private String department;

    @Schema(description = "User's password", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must contain at least 1 uppercase letter, 1 number, and 1 special character"
    )
    private String password;

    @Schema(description = "User's role", example = "USER")
    private String role = "USER";
}
