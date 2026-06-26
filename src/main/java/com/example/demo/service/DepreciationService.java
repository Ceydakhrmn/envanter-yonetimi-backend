package com.example.demo.service;

import com.example.demo.dto.DepreciationResponse;
import com.example.demo.dto.DepreciationSummaryResponse;
import com.example.demo.entity.Asset;
import com.example.demo.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepreciationService {

    private final AssetRepository assetRepository;

    private static final Map<Asset.Category, Integer> DEFAULT_LIFE_YEARS = Map.of(
            Asset.Category.HARDWARE,           5,
            Asset.Category.SOFTWARE_LICENSE,   3,
            Asset.Category.API_SUBSCRIPTION,   1,
            Asset.Category.SAAS_TOOL,          1,
            Asset.Category.OFFICE_EQUIPMENT,   7
    );

    public DepreciationResponse calculate(Asset asset) {
        if (asset.getPurchasePrice() == null || asset.getPurchaseDate() == null) {
            return null;
        }

        int lifeYears = (asset.getUsefulLifeYears() != null && asset.getUsefulLifeYears() > 0)
                ? asset.getUsefulLifeYears()
                : DEFAULT_LIFE_YEARS.getOrDefault(asset.getCategory(), 5);

        BigDecimal price = asset.getPurchasePrice();
        BigDecimal annual = price.divide(BigDecimal.valueOf(lifeYears), 2, RoundingMode.HALF_UP);

        long daysElapsed = ChronoUnit.DAYS.between(asset.getPurchaseDate(), LocalDate.now());
        double yearsElapsed = Math.max(0, daysElapsed / 365.25);

        BigDecimal depreciated = annual.multiply(BigDecimal.valueOf(yearsElapsed))
                .setScale(2, RoundingMode.HALF_UP);
        depreciated = depreciated.min(price);

        BigDecimal currentValue = price.subtract(depreciated).max(BigDecimal.ZERO);
        double percent = price.compareTo(BigDecimal.ZERO) > 0
                ? depreciated.divide(price, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        return new DepreciationResponse(
                asset.getId(),
                asset.getName(),
                price,
                currentValue,
                annual,
                depreciated,
                Math.min(100, percent),
                Math.round(yearsElapsed * 100.0) / 100.0,
                lifeYears,
                currentValue.compareTo(BigDecimal.ZERO) == 0
        );
    }

    public DepreciationSummaryResponse getSummary() {
        List<Asset> assets = assetRepository.findAll();

        List<DepreciationResponse> calculable = assets.stream()
                .map(this::calculate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal totalPurchase = calculable.stream()
                .map(DepreciationResponse::getPurchasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrent = calculable.stream()
                .map(DepreciationResponse::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepr = totalPurchase.subtract(totalCurrent);

        double overallPercent = totalPurchase.compareTo(BigDecimal.ZERO) > 0
                ? totalDepr.divide(totalPurchase, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        long fullyDeprCount = calculable.stream().filter(DepreciationResponse::isFullyDepreciated).count();

        return new DepreciationSummaryResponse(
                totalPurchase,
                totalCurrent,
                totalDepr,
                Math.min(100, overallPercent),
                (int) fullyDeprCount,
                calculable.size(),
                calculable
        );
    }
}
