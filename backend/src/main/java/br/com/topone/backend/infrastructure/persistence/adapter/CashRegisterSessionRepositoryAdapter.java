package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.CashRegisterSession;
import br.com.topone.backend.domain.model.CashRegisterSessionStatus;
import br.com.topone.backend.domain.repository.CashRegisterSessionRepository;
import br.com.topone.backend.infrastructure.persistence.entity.CashRegisterSessionEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.CashRegisterSessionJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CashRegisterSessionRepositoryAdapter implements CashRegisterSessionRepository {

    private final CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository;

    public CashRegisterSessionRepositoryAdapter(CashRegisterSessionJpaRepository cashRegisterSessionJpaRepository) {
        this.cashRegisterSessionJpaRepository = cashRegisterSessionJpaRepository;
    }

    @Override
    public Optional<CashRegisterSession> findOpenByUserId(UUID userId) {
        return cashRegisterSessionJpaRepository.findFirstByUserIdAndStatusOrderByOpenedAtDesc(userId, CashRegisterSessionStatus.OPEN)
                .map(this::toDomain);
    }

    @Override
    public Optional<CashRegisterSession> findOpenById(UUID sessionId) {
        return cashRegisterSessionJpaRepository.findByIdAndStatus(sessionId, CashRegisterSessionStatus.OPEN)
                .map(this::toDomain);
    }

    @Override
    public List<CashRegisterSession> findAllOpenSessions() {
        return cashRegisterSessionJpaRepository.findByStatusOrderByOpenedAtDesc(CashRegisterSessionStatus.OPEN).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CashRegisterSession> findOpenedBetween(Instant fromInclusive, Instant toExclusive) {
        return cashRegisterSessionJpaRepository.findOpenedBetween(fromInclusive, toExclusive).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countOpenSessions() {
        return cashRegisterSessionJpaRepository.countByStatus(CashRegisterSessionStatus.OPEN);
    }

    @Override
    public CashRegisterSession save(CashRegisterSession session) {
        var entity = session.getId() != null
                ? cashRegisterSessionJpaRepository.findById(session.getId()).orElseThrow()
                : new CashRegisterSessionEntity();

        entity.setUserId(session.getUserId());
        entity.setOpeningAmount(session.getOpeningAmount());
        entity.setClosedAt(session.getClosedAt());
        entity.setClosingAmount(session.getClosingAmount());
        entity.setStatus(session.getStatus());

        var saved = cashRegisterSessionJpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    private CashRegisterSession toDomain(CashRegisterSessionEntity entity) {
        return CashRegisterSession.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .openingAmount(entity.getOpeningAmount())
                .openedAt(entity.getOpenedAt())
                .closedAt(entity.getClosedAt())
                .closingAmount(entity.getClosingAmount())
                .status(entity.getStatus())
                .build();
    }
}
