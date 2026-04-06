package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.util.UUID;

public record UserListResponse(
        UUID id,
        String email,
        String name,
        String provider,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
