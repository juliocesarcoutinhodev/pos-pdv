package br.com.topone.backend.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListUsersResult(
        List<UserSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record UserSummary(
            UUID id,
            String email,
            String name,
            String provider,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
    }
}
