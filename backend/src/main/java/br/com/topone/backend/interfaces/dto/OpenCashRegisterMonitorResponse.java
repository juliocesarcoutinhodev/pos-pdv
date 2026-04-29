package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OpenCashRegisterMonitorResponse(
        UUID sessionId,
        UUID userId,
        String userName,
        CashRegisterSessionStatus status,
        Instant openedAt,
        Instant closedAt,
        BigDecimal cashBalance,
        BigDecimal closingAmount,
        BigDecimal differenceAmount
) {
}
