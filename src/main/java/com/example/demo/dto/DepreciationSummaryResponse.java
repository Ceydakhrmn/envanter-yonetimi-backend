package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepreciationSummaryResponse {
    private BigDecimal totalPurchaseValue;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalDepreciation;
    private double overallDepreciationPercent;
    private int fullyDepreciatedCount;
    private int calculableAssetCount;
    private List<DepreciationResponse> assets;
}
