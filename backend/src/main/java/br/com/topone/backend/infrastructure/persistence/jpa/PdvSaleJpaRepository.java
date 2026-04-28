package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PdvSaleJpaRepository extends JpaRepository<PdvSaleEntity, UUID> {

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount), 0)
            FROM PdvSaleEntity s
            WHERE s.cashRegisterSession.id = :sessionId
            """)
    BigDecimal sumTotalAmountBySession(@Param("sessionId") UUID sessionId);

    long countByCashRegisterSessionId(UUID sessionId);

    @Query("""
            SELECT COALESCE(SUM(s.paidAmount - s.changeAmount), 0)
            FROM PdvSaleEntity s
            WHERE s.cashRegisterSession.id = :sessionId
              AND s.paymentMethod = :method
            """)
    BigDecimal sumCashNetBySessionAndMethod(@Param("sessionId") UUID sessionId, @Param("method") PdvPaymentMethod method);

    @Query("""
            SELECT s.paymentMethod, COALESCE(SUM(s.totalAmount), 0), COUNT(s.id)
            FROM PdvSaleEntity s
            WHERE s.cashRegisterSession.id = :sessionId
            GROUP BY s.paymentMethod
            """)
    List<Object[]> sumTotalAmountGroupedByPaymentMethod(@Param("sessionId") UUID sessionId);

    @Query("""
            SELECT s
            FROM PdvSaleEntity s
            WHERE s.cashRegisterSession.id = :sessionId
            ORDER BY s.createdAt DESC
            """)
    List<PdvSaleEntity> findRecentBySession(@Param("sessionId") UUID sessionId, Pageable pageable);
}
