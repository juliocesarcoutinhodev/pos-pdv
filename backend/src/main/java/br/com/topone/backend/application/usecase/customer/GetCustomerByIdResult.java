package br.com.topone.backend.application.usecase.customer;

import java.time.Instant;
import java.util.UUID;

public record GetCustomerByIdResult(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressResult address,
        String imageId,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
