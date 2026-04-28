package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;

public record CashRegisterPaymentSummaryResponse(
        PdvPaymentMethod paymentMethod,
        BigDecimal totalAmount,
        long salesCount
) {
}
