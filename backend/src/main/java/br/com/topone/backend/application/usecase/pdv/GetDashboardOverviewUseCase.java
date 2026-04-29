package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDashboardOverviewUseCase {

    private static final int RECENT_SALES_LIMIT = 5;
    private static final int TOP_PRODUCTS_LIMIT = 6;
    private static final int TOP_PRODUCTS_WINDOW_DAYS = 30;

    private final PdvSaleRepository pdvSaleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final CashRegisterSessionRepository cashRegisterSessionRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResult execute() {
        var zone = ZoneId.systemDefault();
        var today = LocalDate.now(zone);
        var todayStart = toStartInstant(today, zone);
        var tomorrowStart = toStartInstant(today.plusDays(1), zone);
        var topProductsStart = toStartInstant(today.minusDays(TOP_PRODUCTS_WINDOW_DAYS - 1L), zone);

        var todayAmount = normalizeMoney(pdvSaleRepository.sumTotalAmountBetween(todayStart, tomorrowStart));
        var todayCount = pdvSaleRepository.countByCreatedAtBetween(todayStart, tomorrowStart);
        var averageTicket = calculateAverageTicket(todayAmount, todayCount);

        var recentSales = pdvSaleRepository.findHistory(
                        new PdvSaleHistoryFilter(null, null, null, null),
                        0,
                        RECENT_SALES_LIMIT,
                        new PageSort("createdAt", SortDirection.DESC)
                ).content().stream()
                .map(sale -> new DashboardOverviewResult.DashboardRecentSaleResult(
                        sale.getId(),
                        sale.getUserId(),
                        resolveUserName(sale.getUserId()),
                        sale.getPaymentMethod(),
                        normalizeMoney(sale.getTotalAmount()),
                        sale.getItems() != null ? sale.getItems().size() : 0,
                        sale.getCreatedAt()
                ))
                .toList();

        var topProducts = pdvSaleRepository.listTopSellingProductsBetween(topProductsStart, tomorrowStart, TOP_PRODUCTS_LIMIT).stream()
                .map(item -> new DashboardOverviewResult.DashboardTopProductResult(
                        item.productId(),
                        item.sku(),
                        item.name(),
                        normalizeQuantity(item.totalQuantity()),
                        normalizeMoney(item.totalAmount())
                ))
                .toList();

        var paymentSummary = pdvSaleRepository.listTotalsByPaymentMethodBetween(topProductsStart, tomorrowStart).stream()
                .map(item -> new DashboardOverviewResult.DashboardPaymentSummaryResult(
                        item.paymentMethod(),
                        normalizeMoney(item.totalAmount()),
                        item.salesCount()
                ))
                .toList();

        return new DashboardOverviewResult(
                todayAmount,
                todayCount,
                averageTicket,
                userRepository.countActiveUsers(),
                customerRepository.countActiveCustomers(),
                supplierRepository.countActiveSuppliers(),
                productRepository.countActiveProducts(),
                productRepository.countLowStockProducts(),
                cashRegisterSessionRepository.countOpenSessions(),
                recentSales,
                topProducts,
                paymentSummary
        );
    }

    private String resolveUserName(UUID userId) {
        if (userId == null) {
            return "Usuário não informado";
        }

        return userRepository.findById(userId)
                .map(user -> user.getName())
                .orElse("Usuário não encontrado");
    }

    private Instant toStartInstant(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    private BigDecimal calculateAverageTicket(BigDecimal totalAmount, long count) {
        if (count <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return normalizeMoney(totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
