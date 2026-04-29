package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.infrastructure.persistence.entity.CashRegisterSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CashRegisterSessionJpaRepository extends JpaRepository<CashRegisterSessionEntity, UUID> {

    Optional<CashRegisterSessionEntity> findFirstByUserIdAndStatusOrderByOpenedAtDesc(UUID userId, CashRegisterSessionStatus status);

    Optional<CashRegisterSessionEntity> findByIdAndStatus(UUID id, CashRegisterSessionStatus status);

    List<CashRegisterSessionEntity> findByStatusOrderByOpenedAtDesc(CashRegisterSessionStatus status);

    @Query("""
            SELECT s
            FROM CashRegisterSessionEntity s
            WHERE s.openedAt >= :fromInclusive
              AND s.openedAt < :toExclusive
            ORDER BY s.openedAt DESC
            """)
    List<CashRegisterSessionEntity> findOpenedBetween(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    long countByStatus(CashRegisterSessionStatus status);
}
