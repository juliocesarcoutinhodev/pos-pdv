package br.com.topone.backend.application.usecase.supplier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListSuppliersResult(
        List<SupplierSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record SupplierSummary(
            UUID id,
            String name,
            String taxId,
            String email,
            String phone,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
    }
}
