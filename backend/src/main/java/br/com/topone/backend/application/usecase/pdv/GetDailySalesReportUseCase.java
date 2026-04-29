package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.repository.PdvSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetDailySalesReportUseCase {

    private static final int TOP_PRODUCTS_LIMIT = 10;

    private final PdvSaleRepository pdvSaleRepository;

    @Transactional(readOnly = true)
    public DailySalesReportResult execute(LocalDate referenceDate) {
        var zone = ZoneId.systemDefault();
        var date = referenceDate != null ? referenceDate : LocalDate.now(zone);
        var fromInclusive = date.atStartOfDay(zone).toInstant();
        var toExclusive = date.plusDays(1).atStartOfDay(zone).toInstant();

        var totalAmount = normalizeMoney(pdvSaleRepository.sumTotalAmountBetween(fromInclusive, toExclusive));
        var salesCount = pdvSaleRepository.countByCreatedAtBetween(fromInclusive, toExclusive);
        var averageTicket = salesCount > 0
                ? normalizeMoney(totalAmount.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        var paymentSummary = pdvSaleRepository.listTotalsByPaymentMethodBetween(fromInclusive, toExclusive).stream()
                .map(item -> new DailySalesReportResult.DailySalesPaymentSummaryResult(
                        item.paymentMethod(),
                        normalizeMoney(item.totalAmount()),
                        item.salesCount()
                ))
                .toList();

        var hourlySummary = pdvSaleRepository.listHourlyTotalsBetween(fromInclusive, toExclusive).stream()
                .map(item -> new DailySalesReportResult.DailySalesHourlySummaryResult(
                        item.hourOfDay(),
                        normalizeMoney(item.totalAmount()),
                        item.salesCount()
                ))
                .toList();

        var topProducts = pdvSaleRepository.listTopSellingProductsBetween(fromInclusive, toExclusive, TOP_PRODUCTS_LIMIT).stream()
                .map(item -> new DailySalesReportResult.DailySalesTopProductResult(
                        item.productId(),
                        item.sku(),
                        item.name(),
                        normalizeQuantity(item.totalQuantity()),
                        normalizeMoney(item.totalAmount())
                ))
                .toList();

        return new DailySalesReportResult(
                date,
                salesCount,
                totalAmount,
                averageTicket,
                paymentSummary,
                hourlySummary,
                topProducts
        );
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeQuantity(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP);
    }
}
