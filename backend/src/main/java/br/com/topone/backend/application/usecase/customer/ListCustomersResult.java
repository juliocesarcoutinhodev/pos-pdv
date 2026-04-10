package br.com.topone.backend.application.usecase.customer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListCustomersResult(
        List<CustomerSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record CustomerSummary(
            UUID id,
            String name,
            String taxId,
            String email,
            String phone,
            String imageId,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
    }
}
