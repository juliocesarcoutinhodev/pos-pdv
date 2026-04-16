package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.CashMovement;
import br.com.topone.backend.domain.model.CashMovementType;
import br.com.topone.backend.domain.repository.CashMovementRepository;
import br.com.topone.backend.infrastructure.persistence.entity.CashMovementEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.CashMovementJpaRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.CashRegisterSessionJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public class CashMovementRepositoryAdapter implements CashMovementRepository {

    private final CashMovementJpaRepository cashMovementJpaRepository;
    private final CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository;

    public CashMovementRepositoryAdapter(
            CashMovementJpaRepository cashMovementJpaRepository,
            CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository
    ) {
        this.cashMovementJpaRepository = cashMovementJpaRepository;
        this.cashRegisterSessionJpaRepository = cashRegisterSessionJpaRepository;
    }

    @Override
    public CashMovement save(CashMovement movement) {
        var session = cashRegisterSessionJpaRepository.findById(movement.getCashRegisterSessionId()).orElseThrow();
        var entity = CashMovementEntity.builder()
                .cashRegisterSession(session)
                .userId(movement.getUserId())
                .type(movement.getType())
                .amount(movement.getAmount())
                .note(movement.getNote())
                .build();

        var saved = cashMovementJpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public BigDecimal sumAmountBySessionAndType(UUID sessionId, CashMovementType type) {
        return cashMovementJpaRepository.sumAmountBySessionAndType(sessionId, type);
    }

    private CashMovement toDomain(CashMovementEntity entity) {
        return CashMovement.builder()
                .id(entity.getId())
                .cashRegisterSessionId(entity.getCashRegisterSession().getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

