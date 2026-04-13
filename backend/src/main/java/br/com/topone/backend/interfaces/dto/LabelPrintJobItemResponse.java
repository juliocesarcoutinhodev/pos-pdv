package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LabelPrintJobItemResponse(
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
