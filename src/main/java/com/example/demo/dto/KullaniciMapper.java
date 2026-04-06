package com.example.demo.dto;

import com.example.demo.entity.Kullanici;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KullaniciMapper {
	private final PasswordEncoder passwordEncoder;

	public KullaniciResponseDTO toResponseDTO(Kullanici entity) {
		if (entity == null) {
			return null;
		}
		KullaniciResponseDTO dto = new KullaniciResponseDTO();
		dto.setId(entity.getId());
		dto.setFirstName(entity.getFirstName());
		dto.setLastName(entity.getLastName());
		dto.setEmail(entity.getEmail());
		dto.setDepartment(entity.getDepartment());
		dto.setRegistrationDate(entity.getRegistrationDate());
		dto.setActive(entity.getActive());
		dto.setProfilePhoto(entity.getProfilePhoto());
		dto.setLastLoginDate(entity.getLastLoginDate());
		dto.setRole(entity.getRole() != null ? entity.getRole().name() : "USER");
		return dto;
	}

	public List<KullaniciResponseDTO> toResponseDTOList(List<Kullanici> entities) {
		if (entities == null) {
			return null;
		}
		return entities.stream()
				.map(this::toResponseDTO)
				.collect(Collectors.toList());
	}

	public Kullanici toEntity(KullaniciRequestDTO dto) {
		if (dto == null) {
			return null;
		}
		Kullanici entity = new Kullanici();
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		entity.setDepartment(dto.getDepartment());
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity.setRegistrationDate(LocalDateTime.now());
		entity.setActive(true);
		if (dto.getRole() != null && !dto.getRole().isEmpty()) {
			try {
				entity.setRole(Kullanici.Role.valueOf(dto.getRole()));
			} catch (IllegalArgumentException e) {
				entity.setRole(Kullanici.Role.USER);
			}
		} else {
			entity.setRole(Kullanici.Role.USER);
		}
		return entity;
	}

	public void updateEntityFromDTO(KullaniciRequestDTO dto, Kullanici entity) {
		if (dto == null || entity == null) {
			return;
		}
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		entity.setDepartment(dto.getDepartment());
		if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
			entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		}
		if (dto.getRole() != null && !dto.getRole().isEmpty()) {
			try {
				entity.setRole(Kullanici.Role.valueOf(dto.getRole()));
			} catch (IllegalArgumentException e) {
				entity.setRole(Kullanici.Role.USER);
			}
		}
	}
}
