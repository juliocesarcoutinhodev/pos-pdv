package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailySalesReportResult(
        LocalDate referenceDate,
        long salesCount,
        BigDecimal totalAmount,
        BigDecimal averageTicket,
        List<DailySalesPaymentSummaryResult> paymentSummary,
        List<DailySalesHourlySummaryResult> hourlySummary,
        List<DailySalesTopProductResult> topProducts
) {
    public record DailySalesPaymentSummaryResult(
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    public record DailySalesHourlySummaryResult(
            int hourOfDay,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    public record DailySalesTopProductResult(
            UUID productId,
            String sku,
            String name,
            BigDecimal totalQuantity,
            BigDecimal totalAmount
    ) {
    }
}
