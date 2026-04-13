package br.com.topone.backend.application.usecase.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateProductResult(
        UUID id,
        String sku,
        String barcode,
        String name,
        String description,
        String brand,
        String category,
        UUID supplierId,
        String unit,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal marginPercentage,
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
        Instant createdAt
) {
}
