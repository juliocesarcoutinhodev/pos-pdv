package br.com.topone.backend.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashRegisterStatusResponse(
        UUID sessionId,
        BigDecimal openingAmount,
        Instant openedAt,
        BigDecimal suppliesAmount,
        BigDecimal withdrawalsAmount,
        BigDecimal salesAmount,
        BigDecimal cashBalance
) {
}

