package br.com.topone.backend.application.usecase;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateAdminUserResult(
        UUID id,
        String email,
        String name,
        Set<String> roles,
        Instant createdAt
) {
}
