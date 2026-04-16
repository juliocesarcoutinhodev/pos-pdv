package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePdvSaleCommand(
        UUID userId,
        PdvPaymentMethod paymentMethod,
        BigDecimal discountAmount,
        BigDecimal paidAmount,
        String notes,
        List<CreatePdvSaleItemCommand> items
) {
}

