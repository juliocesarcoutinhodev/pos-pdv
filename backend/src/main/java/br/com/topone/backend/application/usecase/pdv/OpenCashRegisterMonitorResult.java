package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OpenCashRegisterMonitorResult(
        UUID sessionId,
        UUID userId,
        String userName,
        CashRegisterSessionStatus status,
        Instant openedAt,
        BigDecimal cashBalance
) {
}
