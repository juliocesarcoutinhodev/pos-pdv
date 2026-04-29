package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailySalesReportResponse(
        LocalDate referenceDate,
        long salesCount,
        BigDecimal totalAmount,
        BigDecimal averageTicket,
        List<DailySalesPaymentSummaryResponse> paymentSummary,
        List<DailySalesHourlySummaryResponse> hourlySummary,
        List<DailySalesTopProductResponse> topProducts
) {
    public record DailySalesPaymentSummaryResponse(
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    public record DailySalesHourlySummaryResponse(
            int hourOfDay,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    public record DailySalesTopProductResponse(
            UUID productId,
            String sku,
            String name,
            BigDecimal totalQuantity,
            BigDecimal totalAmount
    ) {
    }
}
