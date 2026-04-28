package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;

public record CashRegisterPaymentSummaryResult(
        PdvPaymentMethod paymentMethod,
        BigDecimal totalAmount,
        long salesCount
) {
}
