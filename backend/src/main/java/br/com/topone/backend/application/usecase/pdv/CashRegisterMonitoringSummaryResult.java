package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CashRegisterMonitoringSummaryResult(
        UUID sessionId,
        UUID userId,
        String userName,
        CashRegisterSessionStatus status,
        Instant openedAt,
        Instant closedAt,
        BigDecimal openingAmount,
        BigDecimal suppliesAmount,
        BigDecimal withdrawalsAmount,
        BigDecimal salesAmount,
        BigDecimal cashBalance,
        BigDecimal closingAmount,
        BigDecimal differenceAmount,
        long salesCount,
        List<CashRegisterPaymentSummaryResult> paymentSummary,
        List<CashRegisterSaleSummaryResult> recentSales
) {
}
