package br.com.topone.backend.application.usecase.supplier;

import java.util.UUID;

public record DeactivateSupplierCommand(
        UUID id
) {
}
