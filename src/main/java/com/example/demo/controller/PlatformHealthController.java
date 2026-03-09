package com.example.demo.controller;

import com.example.demo.dto.MessageResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformHealthController {

    @GetMapping("/")
    public ResponseEntity<MessageResponseDTO> rootHealth() {
        return ResponseEntity.ok(MessageResponseDTO.builder()
                .message("Efsora backend is running")
                .build());
    }
}
