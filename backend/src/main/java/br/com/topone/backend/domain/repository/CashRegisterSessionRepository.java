package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.CashRegisterSession;

import java.util.Optional;
import java.util.UUID;

public interface CashRegisterSessionRepository {

    Optional<CashRegisterSession> findOpenByUserId(UUID userId);

    CashRegisterSession save(CashRegisterSession session);
}

