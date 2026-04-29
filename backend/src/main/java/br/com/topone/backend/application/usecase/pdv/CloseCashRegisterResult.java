package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CloseCashRegisterResult(
        UUID sessionId,
        UUID userId,
        BigDecimal openingAmount,
        BigDecimal suppliesAmount,
        BigDecimal withdrawalsAmount,
        BigDecimal salesAmount,
        BigDecimal expectedCashAmount,
        BigDecimal closingAmount,
        BigDecimal differenceAmount,
        Instant openedAt,
        Instant closedAt
) {
}
