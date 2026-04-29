package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.util.UUID;

public record CloseCashRegisterCommand(
        UUID userId,
        BigDecimal closingAmount
) {
}
