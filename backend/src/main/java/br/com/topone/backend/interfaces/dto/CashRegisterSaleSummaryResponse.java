package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashRegisterSaleSummaryResponse(
        UUID id,
        PdvPaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal changeAmount,
        Instant createdAt,
        int itemsCount
) {
}
