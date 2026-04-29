package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseCashRegisterRequest(
        @NotNull @DecimalMin(value = "0.00") BigDecimal closingAmount
) {
}
