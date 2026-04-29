package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PdvSaleHistoryResult(
        UUID id,
        UUID sessionId,
        UUID userId,
        String userName,
        PdvPaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal changeAmount,
        int itemsCount,
        String notes,
        Instant createdAt
) {
}
