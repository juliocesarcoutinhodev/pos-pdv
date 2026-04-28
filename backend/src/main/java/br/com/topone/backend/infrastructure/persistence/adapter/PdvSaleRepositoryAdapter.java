package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.domain.model.PdvSale;
import br.com.topone.backend.domain.model.PdvSaleItem;
import br.com.topone.backend.domain.repository.PdvSaleRepository;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleEntity;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleItemEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.CashRegisterSessionJpaRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.PdvSaleJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PdvSaleRepositoryAdapter implements PdvSaleRepository {

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
