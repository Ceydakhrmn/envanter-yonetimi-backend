package com.example.demo.service;

import com.example.demo.dto.AssetRequestDTO;
import com.example.demo.dto.AssetResponseDTO;
import com.example.demo.entity.Asset;
import com.example.demo.entity.Kullanici;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final KullaniciRepository kullaniciRepository;

    public List<AssetResponseDTO> getAll() {
        return assetRepository.findAll().stream().map(AssetResponseDTO::from).toList();
    }

    public AssetResponseDTO getById(Long id) {
        return AssetResponseDTO.from(assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found")));
    }

    public AssetResponseDTO create(AssetRequestDTO dto) {
        Asset asset = toEntity(dto, new Asset());
        return AssetResponseDTO.from(assetRepository.save(asset));
    }

    public AssetResponseDTO update(Long id, AssetRequestDTO dto) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        return AssetResponseDTO.from(assetRepository.save(toEntity(dto, asset)));
    }

    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    public List<AssetResponseDTO> getExpiringSoon() {
        return assetRepository.findExpiringBefore(LocalDate.now().plusDays(30))
                .stream().map(AssetResponseDTO::from).toList();
    }

    public List<AssetResponseDTO> search(String q) {
        return assetRepository.search(q).stream().map(AssetResponseDTO::from).toList();
    }

    private Asset toEntity(AssetRequestDTO dto, Asset asset) {
        asset.setName(dto.getName());
        asset.setCategory(dto.getCategory());
        asset.setBrand(dto.getBrand());
        asset.setModel(dto.getModel());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setVendor(dto.getVendor());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setPurchasePrice(dto.getPurchasePrice());
        asset.setRenewalDate(dto.getRenewalDate());
        asset.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        asset.setStatus(dto.getStatus() != null ? dto.getStatus() : Asset.Status.ACTIVE);
        asset.setSeatCount(dto.getSeatCount());
        asset.setAssignedDepartment(dto.getAssignedDepartment());
        asset.setNotes(dto.getNotes());

        if (dto.getAssignedUserId() != null) {
            Kullanici user = kullaniciRepository.findById(dto.getAssignedUserId()).orElse(null);
            asset.setAssignedUser(user);
        } else {
            asset.setAssignedUser(null);
        }

        return asset;
    }
}
