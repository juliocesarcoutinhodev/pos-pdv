package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierDetailResponse(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        AddressResponse address,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
