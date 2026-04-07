package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleDetailResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
