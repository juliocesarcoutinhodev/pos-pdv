package br.com.topone.backend.application.usecase.customer;

import java.time.Instant;
import java.util.UUID;

public record CreateCustomerResult(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressResult address,
        String imageId,
        Instant createdAt
) {
}
