package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashRegisterStatusResult(
        UUID sessionId,
        BigDecimal openingAmount,
        Instant openedAt,
        BigDecimal suppliesAmount,
        BigDecimal withdrawalsAmount,
        BigDecimal salesAmount,
        BigDecimal cashBalance
) {
}

