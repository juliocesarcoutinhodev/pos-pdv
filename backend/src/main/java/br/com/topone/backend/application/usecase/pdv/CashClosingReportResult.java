package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CashClosingReportResult(
        LocalDate referenceDate,
        long totalSessions,
        long openSessions,
        long closedSessions,
        BigDecimal totalOpeningAmount,
        BigDecimal totalSuppliesAmount,
        BigDecimal totalWithdrawalsAmount,
        BigDecimal totalSalesAmount,
        BigDecimal totalCashBalance,
        List<CashClosingSessionResult> sessions
) {
    public record CashClosingSessionResult(
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
            long salesCount
    ) {
    }
}
