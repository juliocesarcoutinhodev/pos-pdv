package br.com.topone.backend.application.usecase.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateUserResult(
        UUID id,
        String email,
        String name,
        String provider,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
