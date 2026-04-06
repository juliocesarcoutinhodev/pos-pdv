package br.com.topone.backend.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant revokedAt;
    private String replacedByTokenHash;
    private Instant createdAt;
    private Instant lastUsedAt;
    private String userAgent;
    private String ipAddress;

    public RefreshToken(UUID userId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isRevoked();
    }

    public void revoke() {
        if (!isRevoked()) {
            this.revokedAt = Instant.now();
        }
    }

    public void replaceBy(String newTokenHash) {
        this.replacedByTokenHash = newTokenHash;
        this.revokedAt = Instant.now();
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }
}
