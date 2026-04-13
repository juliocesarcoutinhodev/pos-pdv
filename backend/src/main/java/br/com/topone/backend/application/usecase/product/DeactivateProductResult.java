package br.com.topone.backend.application.usecase.product;

import java.util.UUID;

public record DeactivateProductResult(
        UUID id,
        boolean active
) {
}
