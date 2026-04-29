package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.domain.model.PdvSale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvSaleRepository {

    PdvSale save(PdvSale sale);

    BigDecimal sumTotalAmountBySession(UUID sessionId);

    long countBySession(UUID sessionId);

    BigDecimal sumCashNetBySession(UUID sessionId);

    List<PaymentMethodTotal> listTotalsByPaymentMethod(UUID sessionId);

    List<PdvSale> findRecentBySession(UUID sessionId, int limit);

    PageResult<PdvSale> findHistory(PdvSaleHistoryFilter filter, int page, int size, PageSort sort);

    Optional<PdvSale> findById(UUID id);

    BigDecimal sumTotalAmountBetween(Instant fromInclusive, Instant toExclusive);

    long countByCreatedAtBetween(Instant fromInclusive, Instant toExclusive);

    List<PaymentMethodTotal> listTotalsByPaymentMethodBetween(Instant fromInclusive, Instant toExclusive);

    List<HourlySalesTotal> listHourlyTotalsBetween(Instant fromInclusive, Instant toExclusive);

    List<TopSellingProductTotal> listTopSellingProductsBetween(Instant fromInclusive, Instant toExclusive, int limit);

    record PaymentMethodTotal(
            PdvPaymentMethod paymentMethod,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    record HourlySalesTotal(
            int hourOfDay,
            BigDecimal totalAmount,
            long salesCount
    ) {
    }

    record TopSellingProductTotal(
            UUID productId,
            String sku,
            String name,
            BigDecimal totalQuantity,
            BigDecimal totalAmount
    ) {
    }
}
