package br.com.topone.backend.application.usecase.supplier;

import java.util.UUID;

public record SupplierContactResult(
        UUID id,
        String name,
        String email,
        String phone
) {
}
