package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PdvSaleResponse(
        UUID id,
        PdvPaymentMethod paymentMethod,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal changeAmount,
        Instant createdAt,
        List<PdvSaleItemResponse> items
) {
}

