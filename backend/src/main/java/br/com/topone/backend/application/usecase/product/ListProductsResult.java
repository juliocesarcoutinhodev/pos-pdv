package br.com.topone.backend.application.usecase.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListProductsResult(
        List<ProductSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record ProductSummary(
            UUID id,
            String sku,
            String barcode,
            String name,
            String category,
            UUID supplierId,
            String brand,
            String unit,
            BigDecimal salePrice,
            BigDecimal stockQuantity,
            BigDecimal minimumStock,
            String imageId,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
    }
}
