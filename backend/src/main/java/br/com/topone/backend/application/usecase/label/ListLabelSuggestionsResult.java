package br.com.topone.backend.application.usecase.label;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListLabelSuggestionsResult(
        List<ProductSuggestion> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record ProductSuggestion(
            UUID id,
            String sku,
            String barcode,
            String name,
            String category,
            String unit,
            BigDecimal salePrice,
            BigDecimal promotionalPrice,
            Instant createdAt
    ) {
    }
}
