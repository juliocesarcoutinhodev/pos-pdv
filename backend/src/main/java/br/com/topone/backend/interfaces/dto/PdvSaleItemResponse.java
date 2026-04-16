package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PdvSaleItemResponse(
        UUID productId,
        String sku,
        String barcode,
        String name,
        String unit,
        BigDecimal unitPrice,
        BigDecimal quantity,
        BigDecimal lineTotal
) {
}

