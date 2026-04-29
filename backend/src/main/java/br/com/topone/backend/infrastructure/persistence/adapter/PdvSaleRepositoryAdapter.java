package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.domain.model.PdvSale;
import br.com.topone.backend.domain.model.PdvSaleItem;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.domain.repository.PdvSaleHistoryFilter;
import br.com.topone.backend.domain.repository.SortDirection;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleEntity;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleItemEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.CashRegisterSessionJpaRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.PdvSaleJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PdvSaleRepositoryAdapter implements PdvSaleRepository {

    private static final Instant DEFAULT_HISTORY_FROM = Instant.EPOCH;
    private static final Instant DEFAULT_HISTORY_TO = Instant.parse("9999-12-31T23:59:59Z");

    private final PdvSaleJpaRepository pdvSaleJpaRepository;
    private final CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository;

    public PdvSaleRepositoryAdapter(
            PdvSaleJpaRepository pdvSaleJpaRepository,
            CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository
    ) {
        this.pdvSaleJpaRepository = pdvSaleJpaRepository;
        this.cashRegisterSessionJpaRepository = cashRegisterSessionJpaRepository;
    }

    @Override
    public PdvSale save(PdvSale sale) {
        var session = cashRegisterSessionJpaRepository.findById(sale.getCashRegisterSessionId()).orElseThrow();
        var entity = new PdvSaleEntity();
        entity.setCashRegisterSession(session);
        entity.setUserId(sale.getUserId());
        entity.setPaymentMethod(sale.getPaymentMethod());
        entity.setSubtotalAmount(sale.getSubtotalAmount());
        entity.setDiscountAmount(sale.getDiscountAmount());
        entity.setTotalAmount(sale.getTotalAmount());
        entity.setPaidAmount(sale.getPaidAmount());
        entity.setChangeAmount(sale.getChangeAmount());
        entity.setNotes(sale.getNotes());

        entity.getItems().clear();
        for (var item : sale.getItems()) {
            var itemEntity = PdvSaleItemEntity.builder()
                    .sale(entity)
                    .productId(item.getProductId())
                    .sku(item.getSku())
                    .barcode(item.getBarcode())
                    .name(item.getName())
                    .unit(item.getUnit())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(item.getLineTotal())
                    .build();
            entity.getItems().add(itemEntity);
        }

        var saved = pdvSaleJpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public BigDecimal sumTotalAmountBySession(UUID sessionId) {
        return pdvSaleJpaRepository.sumTotalAmountBySession(sessionId);
    }

    @Override
    public long countBySession(UUID sessionId) {
        return pdvSaleJpaRepository.countByCashRegisterSessionId(sessionId);
    }

    @Override
    public BigDecimal sumCashNetBySession(UUID sessionId) {
        return pdvSaleJpaRepository.sumCashNetBySessionAndMethod(sessionId, PdvPaymentMethod.CASH);
    }

    @Override
    public List<PaymentMethodTotal> listTotalsByPaymentMethod(UUID sessionId) {
        return pdvSaleJpaRepository.sumTotalAmountGroupedByPaymentMethod(sessionId).stream()
                .map(row -> new PaymentMethodTotal(
                        (PdvPaymentMethod) row[0],
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<PdvSale> findRecentBySession(UUID sessionId, int limit) {
        return pdvSaleJpaRepository.findRecentBySession(sessionId, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<PdvSale> findHistory(PdvSaleHistoryFilter filter, int page, int size, PageSort sort) {
        var sortSpec = sort != null && sort.isSorted()
                ? Sort.by(
                sort.direction() == SortDirection.DESC
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sort.field())
                : Sort.by(Sort.Direction.DESC, "createdAt");

        var springPage = PageRequest.of(page, size, sortSpec);
        var fromInclusive = filter.fromInclusive() != null ? filter.fromInclusive() : DEFAULT_HISTORY_FROM;
        var toExclusive = filter.toExclusive() != null ? filter.toExclusive() : DEFAULT_HISTORY_TO;
        var result = pdvSaleJpaRepository.findHistory(
                filter.userId(),
                filter.paymentMethod(),
                fromInclusive,
                toExclusive,
                springPage
        );

        var content = result.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Optional<PdvSale> findById(UUID id) {
        return pdvSaleJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public BigDecimal sumTotalAmountBetween(Instant fromInclusive, Instant toExclusive) {
        return pdvSaleJpaRepository.sumTotalAmountBetween(fromInclusive, toExclusive);
    }

    @Override
    public long countByCreatedAtBetween(Instant fromInclusive, Instant toExclusive) {
        return pdvSaleJpaRepository.countByCreatedAtBetween(fromInclusive, toExclusive);
    }

    @Override
    public List<PaymentMethodTotal> listTotalsByPaymentMethodBetween(Instant fromInclusive, Instant toExclusive) {
        return pdvSaleJpaRepository.sumTotalsByPaymentMethodBetween(fromInclusive, toExclusive).stream()
                .map(row -> new PaymentMethodTotal(
                        (PdvPaymentMethod) row[0],
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<HourlySalesTotal> listHourlyTotalsBetween(Instant fromInclusive, Instant toExclusive) {
        return pdvSaleJpaRepository.sumHourlyTotalsBetween(fromInclusive, toExclusive).stream()
                .map(row -> new HourlySalesTotal(
                        ((Number) row[0]).intValue(),
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TopSellingProductTotal> listTopSellingProductsBetween(Instant fromInclusive, Instant toExclusive, int limit) {
        var safeLimit = Math.max(1, Math.min(limit, 20));
        return pdvSaleJpaRepository.sumTopSellingProductsBetween(fromInclusive, toExclusive, PageRequest.of(0, safeLimit)).stream()
                .map(row -> new TopSellingProductTotal(
                        (UUID) row[0],
                        (String) row[1],
                        (String) row[2],
                        (BigDecimal) row[3],
                        (BigDecimal) row[4]
                ))
                .toList();
    }

    private PdvSale toDomain(PdvSaleEntity entity) {
        var items = entity.getItems().stream()
                .map(item -> PdvSaleItem.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .sku(item.getSku())
                        .barcode(item.getBarcode())
                        .name(item.getName())
                        .unit(item.getUnit())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return PdvSale.builder()
                .id(entity.getId())
                .cashRegisterSessionId(entity.getCashRegisterSession().getId())
                .userId(entity.getUserId())
                .paymentMethod(entity.getPaymentMethod())
                .subtotalAmount(entity.getSubtotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .totalAmount(entity.getTotalAmount())
                .paidAmount(entity.getPaidAmount())
                .changeAmount(entity.getChangeAmount())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .items(new ArrayList<>(items))
                .build();
    }
}
