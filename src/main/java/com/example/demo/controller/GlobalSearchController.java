package com.example.demo.controller;

import com.example.demo.dto.AssetResponseDTO;
import com.example.demo.dto.KullaniciResponseDTO;
import com.example.demo.dto.KullaniciMapper;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import com.example.demo.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final KullaniciRepository kullaniciRepository;
    private final AssetService assetService;
    private final KullaniciMapper kullaniciMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(Map.of("users", List.of(), "assets", List.of()));
        }

        String query = q.trim();
        Map<String, Object> results = new HashMap<>();

        // Search users by name, email, department
        List<Kullanici> allUsers = kullaniciRepository.findByActiveTrue();
        String lowerQ = query.toLowerCase();
        List<KullaniciResponseDTO> matchedUsers = allUsers.stream()
                .filter(u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(lowerQ)
                        || u.getEmail().toLowerCase().contains(lowerQ)
                        || (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(lowerQ)))
                .limit(10)
                .map(kullaniciMapper::toResponseDTO)
                .toList();

        // Search assets by name, brand, serial number
        List<AssetResponseDTO> matchedAssets = assetService.search(query).stream()
                .limit(10)
                .toList();

        results.put("users", matchedUsers);
        results.put("assets", matchedAssets);

        return ResponseEntity.ok(results);
    }
}
