package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LabelPrintSuggestionResponse(
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
