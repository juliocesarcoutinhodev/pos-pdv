package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductListResponse(
        UUID id,
        String sku,
        String barcode,
        String name,
        String category,
        String brand,
        String unit,
        BigDecimal salePrice,
        BigDecimal stockQuantity,
        String imageId,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
