package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePdvSaleItemRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity
) {
}

