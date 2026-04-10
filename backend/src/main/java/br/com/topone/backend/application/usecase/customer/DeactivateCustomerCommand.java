package br.com.topone.backend.application.usecase.customer;

import java.util.UUID;

public record DeactivateCustomerCommand(
        UUID id
) {
}
