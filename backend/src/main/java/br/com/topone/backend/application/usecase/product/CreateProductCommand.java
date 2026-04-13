package br.com.topone.backend.application.usecase.product;

import java.math.BigDecimal;

public record CreateProductCommand(
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
        String imageId
) {
}
