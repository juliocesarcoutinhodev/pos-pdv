package br.com.topone.backend.application.usecase.role;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListRolesResult(
        List<RoleSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record RoleSummary(
            UUID id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
