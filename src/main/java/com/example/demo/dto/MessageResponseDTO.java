package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic message response")
public class MessageResponseDTO {

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Timestamp when response was generated", example = "2026-03-05T12:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
