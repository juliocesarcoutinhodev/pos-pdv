package br.com.topone.backend.application.usecase.supplier;

import java.time.Instant;
import java.util.UUID;

public record CreateSupplierResult(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        SupplierAddressResult address,
        Instant createdAt
) {
}
