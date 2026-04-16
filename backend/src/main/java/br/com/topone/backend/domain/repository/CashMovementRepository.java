package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.CashMovement;
import br.com.topone.backend.domain.model.CashMovementType;

import java.math.BigDecimal;
import java.util.UUID;

public interface CashMovementRepository {

    CashMovement save(CashMovement movement);

    BigDecimal sumAmountBySessionAndType(UUID sessionId, CashMovementType type);
}

