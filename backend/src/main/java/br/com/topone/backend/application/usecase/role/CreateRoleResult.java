package br.com.topone.backend.application.usecase.role;

import java.time.Instant;
import java.util.UUID;

public record CreateRoleResult(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
}
