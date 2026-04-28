package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.CashRegisterSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterSessionRepository {

    Optional<CashRegisterSession> findOpenByUserId(UUID userId);

    Optional<CashRegisterSession> findOpenById(UUID sessionId);

    List<CashRegisterSession> findAllOpenSessions();

    CashRegisterSession save(CashRegisterSession session);
}
