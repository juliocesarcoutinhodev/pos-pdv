package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.domain.model.PdvPaymentMethod;
import br.com.topone.backend.infrastructure.persistence.entity.PdvSaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Query("""
            SELECT s
            FROM PdvSaleEntity s
            WHERE (:userId IS NULL OR s.userId = :userId)
              AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod)
              AND s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            """)
    Page<PdvSaleEntity> findHistory(@Param("userId") UUID userId,
                                    @Param("paymentMethod") PdvPaymentMethod paymentMethod,
                                    @Param("fromInclusive") Instant fromInclusive,
                                    @Param("toExclusive") Instant toExclusive,
                                    Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount), 0)
            FROM PdvSaleEntity s
            WHERE s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            """)
    BigDecimal sumTotalAmountBetween(@Param("fromInclusive") Instant fromInclusive, @Param("toExclusive") Instant toExclusive);

    @Query("""
            SELECT COUNT(s.id)
            FROM PdvSaleEntity s
            WHERE s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            """)
    long countByCreatedAtBetween(@Param("fromInclusive") Instant fromInclusive, @Param("toExclusive") Instant toExclusive);

    @Query("""
            SELECT s.paymentMethod, COALESCE(SUM(s.totalAmount), 0), COUNT(s.id)
            FROM PdvSaleEntity s
            WHERE s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            GROUP BY s.paymentMethod
            """)
    List<Object[]> sumTotalsByPaymentMethodBetween(@Param("fromInclusive") Instant fromInclusive, @Param("toExclusive") Instant toExclusive);

    @Query("""
            SELECT FUNCTION('HOUR', s.createdAt), COALESCE(SUM(s.totalAmount), 0), COUNT(s.id)
            FROM PdvSaleEntity s
            WHERE s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            GROUP BY FUNCTION('HOUR', s.createdAt)
            ORDER BY FUNCTION('HOUR', s.createdAt)
            """)
    List<Object[]> sumHourlyTotalsBetween(@Param("fromInclusive") Instant fromInclusive, @Param("toExclusive") Instant toExclusive);

    @Query("""
            SELECT i.productId, i.sku, i.name, COALESCE(SUM(i.quantity), 0), COALESCE(SUM(i.lineTotal), 0)
            FROM PdvSaleEntity s
            JOIN s.items i
            WHERE s.createdAt >= :fromInclusive
              AND s.createdAt < :toExclusive
            GROUP BY i.productId, i.sku, i.name
            ORDER BY SUM(i.quantity) DESC
            """)
    List<Object[]> sumTopSellingProductsBetween(@Param("fromInclusive") Instant fromInclusive,
                                                @Param("toExclusive") Instant toExclusive,
                                                Pageable pageable);
}
