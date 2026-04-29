package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardOverviewResponse(
        BigDecimal todaySalesAmount,
        long todaySalesCount,
        BigDecimal todayAverageTicket,
        long activeUsers,
        long activeCustomers,
        long activeSuppliers,
        long activeProducts,
        long lowStockProducts,
        long openCashRegisters,
        List<DashboardRecentSaleResponse> recentSales,
        List<DashboardTopProductResponse> topProducts,
        List<DashboardPaymentSummaryResponse> paymentSummary
) {
    public record DashboardRecentSaleResponse(
            UUID id,
            UUID userId,
            String userName,
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            int itemsCount,
            Instant createdAt
    ) {
    }

    public record DashboardTopProductResponse(
            UUID productId,
            String sku,
            String name,
            BigDecimal totalQuantity,
            BigDecimal totalAmount
    ) {
    }

    public record DashboardPaymentSummaryResponse(
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }
}
