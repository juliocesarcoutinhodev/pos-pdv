package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenCashRegisterCommand(
        UUID userId,
        BigDecimal openingAmount
) {
}

