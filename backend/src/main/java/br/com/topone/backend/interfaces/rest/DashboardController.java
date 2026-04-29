package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.pdv.GetDashboardOverviewUseCase;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.DashboardOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardOverviewUseCase getDashboardOverviewUseCase;

    @GetMapping("/overview")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<DashboardOverviewResponse> getOverview() {
        var result = getDashboardOverviewUseCase.execute();
        return ResponseEntity.ok(new DashboardOverviewResponse(
                result.todaySalesAmount(),
                result.todaySalesCount(),
                result.todayAverageTicket(),
                result.activeUsers(),
                result.activeCustomers(),
                result.activeSuppliers(),
                result.activeProducts(),
                result.lowStockProducts(),
                result.openCashRegisters(),
                result.recentSales().stream()
                        .map(item -> new DashboardOverviewResponse.DashboardRecentSaleResponse(
                                item.id(),
                                item.userId(),
                                item.userName(),
                                item.paymentMethod(),
                                item.totalAmount(),
                                item.itemsCount(),
                                item.createdAt()
                        ))
                        .toList(),
                result.topProducts().stream()
                        .map(item -> new DashboardOverviewResponse.DashboardTopProductResponse(
                                item.productId(),
                                item.sku(),
                                item.name(),
                                item.totalQuantity(),
                                item.totalAmount()
                        ))
                        .toList(),
                result.paymentSummary().stream()
                        .map(item -> new DashboardOverviewResponse.DashboardPaymentSummaryResponse(
                                item.paymentMethod(),
                                item.totalAmount(),
                                item.salesCount()
                        ))
                        .toList()
        ));
    }
}
