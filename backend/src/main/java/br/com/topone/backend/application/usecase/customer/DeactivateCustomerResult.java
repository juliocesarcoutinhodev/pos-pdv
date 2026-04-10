package br.com.topone.backend.application.usecase.customer;

import java.util.UUID;

public record DeactivateCustomerResult(
        UUID id,
        boolean active
) {
}
