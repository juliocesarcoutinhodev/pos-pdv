package br.com.topone.backend.application.usecase.supplier;

import java.time.Instant;
import java.util.UUID;

public record UpdateSupplierResult(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        SupplierAddressResult address,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
