package com.example.demo.dto;

import com.example.demo.entity.Asset;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetRequestDTO {
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
    private String assignedDepartment;
    private String notes;
}
