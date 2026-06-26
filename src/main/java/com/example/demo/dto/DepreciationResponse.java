package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepreciationResponse {
    private Long assetId;
    private String assetName;
    private BigDecimal purchasePrice;
    private BigDecimal currentValue;
    private BigDecimal annualDepreciation;
    private BigDecimal totalDepreciation;
    private double depreciationPercent;
    private double yearsElapsed;
    private int usefulLifeYears;
    private boolean fullyDepreciated;
}
