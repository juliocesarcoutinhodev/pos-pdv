package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.CashRegisterSession;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

public interface CashRegisterSessionRepository {

    Optional<CashRegisterSession> findOpenByUserId(UUID userId);

    Optional<CashRegisterSession> findOpenById(UUID sessionId);

    List<CashRegisterSession> findAllOpenSessions();

    List<CashRegisterSession> findAllSessions();

    Optional<CashRegisterSession> findById(UUID sessionId);

    List<CashRegisterSession> findOpenedBetween(Instant fromInclusive, Instant toExclusive);

    long countOpenSessions();

    CashRegisterSession save(CashRegisterSession session);
}
