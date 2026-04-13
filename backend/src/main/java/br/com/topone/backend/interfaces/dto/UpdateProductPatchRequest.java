package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductPatchRequest(
        @Size(max = 60) String sku,
        @Size(max = 20) String barcode,
        @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @Size(max = 120) String brand,
        @Size(max = 100) String category,
        UUID supplierId,
        @Size(max = 10) String unit,
        @PositiveOrZero BigDecimal costPrice,
        @PositiveOrZero BigDecimal salePrice,
        @PositiveOrZero BigDecimal marginPercentage,
        @PositiveOrZero BigDecimal promotionalPrice,
        @PositiveOrZero BigDecimal stockQuantity,
        @PositiveOrZero BigDecimal minimumStock,
        @Size(max = 8) String ncm,
        @Size(max = 7) String cest,
        @Size(max = 4) String cfop,
        @Size(max = 2) String taxOrigin,
        @Size(max = 10) String taxSituation,
        @PositiveOrZero BigDecimal icmsRate,
        @Size(max = 4) String pisSituation,
        @PositiveOrZero BigDecimal pisRate,
        @Size(max = 4) String cofinsSituation,
        @PositiveOrZero BigDecimal cofinsRate,
        @Size(max = 120) String imageId,
        Boolean active
) {
}
