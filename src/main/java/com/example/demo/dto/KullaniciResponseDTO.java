package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KullaniciResponseDTO {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Ceyda")
    private String firstName;

    @Schema(example = "Efsora")
    private String lastName;

    @Schema(example = "ceyda@efsora.com")
    private String email;

    @Schema(example = "IT")
    private String department;

    @Schema(description = "User registration date", example = "2024-05-01T12:00:00")
    private java.time.LocalDateTime registrationDate;

    @Schema(description = "Is user active?", example = "true")
    private Boolean active;
}
