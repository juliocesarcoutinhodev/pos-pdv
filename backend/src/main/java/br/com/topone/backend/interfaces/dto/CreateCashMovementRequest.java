package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.CashMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCashMovementRequest(
        @NotNull CashMovementType type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(max = 255) String note
) {
}

