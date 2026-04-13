package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String sku,
        String barcode,
        String name,
        String description,
        String brand,
        String category,
        String unit,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal promotionalPrice,
        BigDecimal stockQuantity,
        BigDecimal minimumStock,
        String ncm,
        String cest,
        String cfop,
        String taxOrigin,
        String taxSituation,
        BigDecimal icmsRate,
        String pisSituation,
        BigDecimal pisRate,
        String cofinsSituation,
        BigDecimal cofinsRate,
        String imageId,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
