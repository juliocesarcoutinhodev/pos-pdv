package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashMovementResult(
        UUID id,
        CashMovementType type,
        BigDecimal amount,
        String note,
        Instant createdAt,
        BigDecimal cashBalance
) {
}

