package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePdvSaleItemCommand(
        UUID productId,
        BigDecimal quantity
) {
}

