package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findById(UUID id);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUserId(UUID userId);
    List<RefreshToken> findActiveByUserId(UUID userId);
    void deleteById(UUID id);
    void revokeAllByUserId(UUID userId);
}
