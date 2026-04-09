package com.example.demo.dto;

import com.example.demo.entity.Asset;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AssetResponseDTO {
    private Long id;
    private String name;
    private Asset.Category category;
    private String brand;
    private String model;
    private String serialNumber;
    private String vendor;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate renewalDate;
    private LocalDate warrantyExpiryDate;
    private Asset.Status status;
    private Integer seatCount;
    private Long assignedUserId;
    private String assignedUserName;
    private String assignedDepartment;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetResponseDTO from(Asset asset) {
        AssetResponseDTO dto = new AssetResponseDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setCategory(asset.getCategory());
        dto.setBrand(asset.getBrand());
        dto.setModel(asset.getModel());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setVendor(asset.getVendor());
        dto.setPurchaseDate(asset.getPurchaseDate());
        dto.setPurchasePrice(asset.getPurchasePrice());
        dto.setRenewalDate(asset.getRenewalDate());
        dto.setWarrantyExpiryDate(asset.getWarrantyExpiryDate());
        dto.setStatus(asset.getStatus());
        dto.setSeatCount(asset.getSeatCount());
        dto.setAssignedDepartment(asset.getAssignedDepartment());
        dto.setNotes(asset.getNotes());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        if (asset.getAssignedUser() != null) {
            dto.setAssignedUserId(asset.getAssignedUser().getId());
            dto.setAssignedUserName(asset.getAssignedUser().getFirstName() + " " + asset.getAssignedUser().getLastName());
        }
        return dto;
    }
}
