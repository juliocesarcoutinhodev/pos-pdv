package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.infrastructure.persistence.entity.CashRegisterSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterSessionJpaRepository extends JpaRepository<CashRegisterSessionEntity, UUID> {

    Optional<CashRegisterSessionEntity> findFirstByUserIdAndStatusOrderByOpenedAtDesc(UUID userId, CashRegisterSessionStatus status);
}

