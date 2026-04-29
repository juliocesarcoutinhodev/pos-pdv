package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.model.PdvPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardOverviewResult(
        BigDecimal todaySalesAmount,
        long todaySalesCount,
        BigDecimal todayAverageTicket,
        long activeUsers,
        long activeCustomers,
        long activeSuppliers,
        long activeProducts,
        long lowStockProducts,
        long openCashRegisters,
        List<DashboardRecentSaleResult> recentSales,
        List<DashboardTopProductResult> topProducts,
        List<DashboardPaymentSummaryResult> paymentSummary
) {
    public record DashboardRecentSaleResult(
            UUID id,
            UUID userId,
            String userName,
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            int itemsCount,
            Instant createdAt
    ) {
    }

    public record DashboardTopProductResult(
            UUID productId,
            String sku,
            String name,
            BigDecimal totalQuantity,
            BigDecimal totalAmount
    ) {
    }

    public record DashboardPaymentSummaryResult(
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }
}
