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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Asset getEntityById(Long id) {
        return assetRepository.findById(id).orElse(null);
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

    public void bulkDelete(List<Long> ids) {
        assetRepository.deleteAllById(ids);
    }

    public List<Map<String, Object>> bulkImport(List<AssetRequestDTO> dtos) {
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        int row = 1;
        for (AssetRequestDTO dto : dtos) {
            Map<String, Object> result = new HashMap<>();
            result.put("row", row++);
            result.put("name", dto.getName());
            try {
                if (dto.getName() == null || dto.getName().isBlank()) {
                    throw new RuntimeException("Name is required");
                }
                Asset asset = toEntity(dto, new Asset());
                assetRepository.save(asset);
                result.put("status", "success");
            } catch (Exception e) {
                result.put("status", "error");
                result.put("message", e.getMessage());
            }
            results.add(result);
        }
        return results;
    }

    public void bulkUpdateStatus(List<Long> ids, Asset.Status status) {
        List<Asset> assets = assetRepository.findAllById(ids);
        assets.forEach(a -> a.setStatus(status));
        assetRepository.saveAll(assets);
    }

    public List<AssetResponseDTO> getExpiringSoon() {
        return assetRepository.findExpiringBefore(LocalDate.now().plusDays(30))
                .stream().map(AssetResponseDTO::from).toList();
    }

    public List<AssetResponseDTO> search(String q) {
        return assetRepository.search(q).stream().map(AssetResponseDTO::from).toList();
    }

    public Map<String, Object> getStats() {
        List<Asset> all = assetRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", all.size());
        stats.put("active", all.stream().filter(a -> a.getStatus() == Asset.Status.ACTIVE).count());
        stats.put("maintenance", all.stream().filter(a -> a.getStatus() == Asset.Status.MAINTENANCE).count());
        stats.put("expired", all.stream().filter(a -> a.getStatus() == Asset.Status.EXPIRED).count());
        stats.put("retired", all.stream().filter(a -> a.getStatus() == Asset.Status.RETIRED).count());

        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(a -> a.getCategory().name(), Collectors.counting()));
        stats.put("byCategory", byCategory);

        long expiringSoon = all.stream()
                .filter(a -> a.getRenewalDate() != null && a.getStatus() == Asset.Status.ACTIVE
                        && a.getRenewalDate().isBefore(LocalDate.now().plusDays(30)))
                .count();
        stats.put("expiringSoon", expiringSoon);

        long totalValue = all.stream()
                .filter(a -> a.getPurchasePrice() != null)
                .mapToLong(a -> a.getPurchasePrice().longValue())
                .sum();
        stats.put("totalValue", totalValue);

        return stats;
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
        asset.setUsefulLifeYears(dto.getUsefulLifeYears());
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
