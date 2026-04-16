package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.util.UUID;

public record PdvProductLookupResult(
        UUID id,
        String sku,
        String barcode,
        String name,
        String unit,
        BigDecimal unitPrice,
        BigDecimal stockQuantity
) {
}

