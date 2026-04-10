package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierListResponse(
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
