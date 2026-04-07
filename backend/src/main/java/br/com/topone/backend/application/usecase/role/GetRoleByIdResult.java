package br.com.topone.backend.application.usecase.role;

import java.time.Instant;
import java.util.UUID;

public record GetRoleByIdResult(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
