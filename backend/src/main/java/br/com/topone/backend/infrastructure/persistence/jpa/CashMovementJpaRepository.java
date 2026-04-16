package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.infrastructure.persistence.entity.CashMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface CashMovementJpaRepository extends JpaRepository<CashMovementEntity, UUID> {

    @Query("""
            SELECT COALESCE(SUM(m.amount), 0)
            FROM CashMovementEntity m
            WHERE m.cashRegisterSession.id = :sessionId
              AND m.type = :type
            """)
    BigDecimal sumAmountBySessionAndType(@Param("sessionId") UUID sessionId, @Param("type") CashMovementType type);
}

