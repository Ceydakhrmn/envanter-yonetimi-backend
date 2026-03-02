package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Response DTO - Used for returning user data to clients
 * Contains all non-sensitive fields including technical metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User response data")
public class KullaniciResponseDTO {

    @Schema(description = "User ID (auto-generated)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "User's first name", example = "Ahmet")
    private String ad;

    @Schema(description = "User's last name", example = "Yılmaz")
    private String soyad;

    @Schema(description = "User's email address (unique)", example = "ahmet@efsora.com")
    private String email;

    @Schema(description = "User's department", example = "IT")
    private String departman;

    @Schema(description = "Registration date (auto-generated)", example = "2026-03-02T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime kayitTarihi;

    @Schema(description = "Is user active? (false = soft deleted)", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean aktif;
}
