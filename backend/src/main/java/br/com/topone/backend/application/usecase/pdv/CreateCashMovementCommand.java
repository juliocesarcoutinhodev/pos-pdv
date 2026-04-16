package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashMovementType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCashMovementCommand(
        UUID userId,
        CashMovementType type,
        BigDecimal amount,
        String note
) {
}

