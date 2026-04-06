package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository, RefreshTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        var entity = mapper.toEntity(refreshToken);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(UUID userId) {
        return mapper.toDomainList(jpaRepository.findByUserId(userId));
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId) {
        return mapper.toDomainList(jpaRepository.findActiveByUserId(userId));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
