package br.com.topone.backend.application.usecase.label;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateLabelPrintJobResult(
        UUID id,
        LocalDate referenceDate,
        int totalProducts,
        int totalLabels,
        Instant createdAt,
        List<Item> items
) {
    public record Item(
            UUID productId,
            String sku,
            String barcode,
            String name,
            String unit,
            BigDecimal salePrice,
            BigDecimal promotionalPrice,
            Integer quantity
    ) {
    }
}
