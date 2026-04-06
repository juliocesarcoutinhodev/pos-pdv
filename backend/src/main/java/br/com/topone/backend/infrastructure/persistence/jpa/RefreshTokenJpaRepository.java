package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findByUserId(UUID userId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.user.id = :userId AND r.revokedAt IS NULL AND r.expiresAt > CURRENT_TIMESTAMP")
    List<RefreshTokenEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = CURRENT_TIMESTAMP WHERE r.user.id = :userId AND r.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
