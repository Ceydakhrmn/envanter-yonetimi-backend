package com.example.demo.dto;

import com.example.demo.entity.Kullanici;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Kullanici entity and DTOs
 * Handles bidirectional mapping between domain models and data transfer objects
 */
@Component
@RequiredArgsConstructor
public class KullaniciMapper {

    private final PasswordEncoder passwordEncoder;

    /**
     * Convert entity to response DTO
     */
    public KullaniciResponseDTO toResponseDTO(Kullanici entity) {
        if (entity == null) {
            return null;
        }
        
        KullaniciResponseDTO dto = new KullaniciResponseDTO();
        dto.setId(entity.getId());
        dto.setAd(entity.getAd());
        dto.setSoyad(entity.getSoyad());
        dto.setEmail(entity.getEmail());
        dto.setDepartman(entity.getDepartman());
        dto.setKayitTarihi(entity.getKayitTarihi());
        dto.setAktif(entity.getAktif());
        return dto;
    }

    /**
     * Convert list of entities to list of response DTOs
     */
    public List<KullaniciResponseDTO> toResponseDTOList(List<Kullanici> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert request DTO to entity (for create operations)
     */
    public Kullanici toEntity(KullaniciRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Kullanici entity = new Kullanici();
        entity.setAd(dto.getAd());
        entity.setSoyad(dto.getSoyad());
        entity.setEmail(dto.getEmail());
        entity.setDepartman(dto.getDepartman());
        entity.setPassword(passwordEncoder.encode(dto.getPassword())); // Hash password before saving
        entity.setKayitTarihi(LocalDateTime.now());
        entity.setAktif(true);
        return entity;
    }

    /**
     * Update existing entity from request DTO (for update operations)
     */
    public void updateEntityFromDTO(KullaniciRequestDTO dto, Kullanici entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        entity.setAd(dto.getAd());
        entity.setSoyad(dto.getSoyad());
        entity.setEmail(dto.getEmail());
        entity.setDepartman(dto.getDepartman());
        
        // Hash password if provided and not already hashed
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        // Do not update: id, kayitTarihi, aktif (these are managed by system)
    }
}
