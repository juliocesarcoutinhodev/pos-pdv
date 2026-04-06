package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDetailResponse(
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
