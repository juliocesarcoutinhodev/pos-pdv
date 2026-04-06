package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Query("SELECT r FROM RefreshTokenEntity r JOIN FETCH r.user WHERE r.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHash(@Param("tokenHash") String tokenHash);

    List<RefreshTokenEntity> findByUserId(UUID userId);

    @Query("SELECT r FROM RefreshTokenEntity r JOIN FETCH r.user WHERE r.user.id = :userId AND r.revokedAt IS NULL AND r.expiresAt > :now")
    List<RefreshTokenEntity> findActiveByUserId(@Param("userId") UUID userId, @Param("now") java.time.Instant now);

    @Query("SELECT r FROM RefreshTokenEntity r JOIN FETCH r.user WHERE r.user.id = :userId AND r.revokedAt IS NULL")
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(@Param("userId") UUID userId);
}
