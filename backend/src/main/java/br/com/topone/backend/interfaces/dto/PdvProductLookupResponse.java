package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PdvProductLookupResponse(
        UUID id,
        String sku,
        String barcode,
        String name,
        String unit,
        BigDecimal unitPrice,
        BigDecimal stockQuantity
) {
}

